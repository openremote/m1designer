package org.openremote.beta.shared.flow;

import org.openremote.beta.server.util.IdentifierUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class FlowDependencyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependencyResolver.class);

    public void populateDependencies(Flow flow, boolean hydrate) {
        flow.clearDependencies();

        List<FlowDependency> superDependencies = new ArrayList<>();
        populateSuperDependencies(flow, 0, superDependencies);
        Collections.reverse(superDependencies);
        flow.setSuperDependencies(superDependencies.toArray(new FlowDependency[superDependencies.size()]));

        List<FlowDependency> subDependencies = new ArrayList<>();
        populateSubDependencies(flow, hydrate, 0, subDependencies);
        flow.setSubDependencies(subDependencies.toArray(new FlowDependency[subDependencies.size()]));
    }

    public void updateDependencies(Flow flow, boolean flowWillBeDeleted) {
        if (flowWillBeDeleted)
            flow.removeProducerConsumerNodes();

        flow.clearDependencies();
        populateDependencies(flow, false);

        for (FlowDependency superDependency : flow.getDirectSuperDependencies()) {
            Flow superFlow = findFlow(superDependency.getId());

            boolean superFlowModified = false;
            boolean superFlowShouldBeStopped= false;

            for (Node subflowNode : superFlow.findSubflowNodes()) {
                if (!subflowNode.getSubflowId().equals(flow.getId()))
                    continue;

                if (flowWillBeDeleted) {
                    // Easy case, the flow will be deleted, so delete subflow node and all its wires
                    superFlow.removeNode(subflowNode);
                    superFlowModified = true;
                    superFlowShouldBeStopped = true;
                } else {

                    // Find slots we no longer have and delete them and their wires
                    Slot[] slotsWithoutPeer = superFlow.findSlotsWithoutPeer(subflowNode, flow);
                    for (Slot slotWithoutPeer : slotsWithoutPeer) {
                        if (superFlow.removeSlot(subflowNode, slotWithoutPeer.getId())) {
                            superFlowModified = true;
                            superFlowShouldBeStopped = true;
                        }
                    }

                    // All other slots which are still valid, update the label
                    for (Slot subflowSlot : subflowNode.getSlots()) {
                        Node peerNode = flow.findOwnerNode(subflowSlot.getPeerId());
                        if (peerNode.getLabel() != null && !peerNode.getLabel().equals(subflowSlot.getLabel())) {
                            subflowSlot.setLabel(peerNode.getLabel());
                            superFlowModified = true;
                            superFlowShouldBeStopped = false;
                        }
                    }

                    // Add new slots for any new consumers/producers
                    List<Slot> newSlots = new ArrayList<>();
                    Node[] consumers = flow.findNodes(Node.TYPE_CONSUMER);
                    for (Node consumer : consumers) {
                        Slot firstSink = consumer.findSlots(Slot.TYPE_SINK)[0];
                        if (subflowNode.findSlotWithPeer(firstSink.getId()) == null)
                            newSlots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSink, consumer.getLabel()));
                    }
                    Node[] producers = flow.findNodes(Node.TYPE_PRODUCER);
                    for (Node producer : producers) {
                        Slot firstSource = producer.findSlots(Slot.TYPE_SOURCE)[0];
                        if (subflowNode.findSlotWithPeer(firstSource.getId()) == null)
                            newSlots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSource, producer.getLabel()));
                    }
                    if (newSlots.size() > 0) {
                        subflowNode.addSlots(newSlots.toArray(new Slot[newSlots.size()]));
                        superFlowModified = true;
                        superFlowShouldBeStopped = false;
                    }
                }
            }

            if (superFlowModified) {
                superFlow.clearDependencies();
                if (superFlowShouldBeStopped)
                    stopFlowIfRunning(superFlow);
                storeFlow(superFlow);
            }
        }
    }

    protected void populateSuperDependencies(Flow flow, int level, List<FlowDependency> dependencyList) {

        Flow[] subflowDependents = findSubflowDependents(flow.getId());

        for (Flow subflowDependent : subflowDependents) {

            // Is this dependency breakable (wires attached?) or already broken (invalid peers?)
            boolean flowHasWiresAttached = false;
            boolean flowHasInvalidPeers = false;

            Node[] subflowNodes = subflowDependent.findSubflowNodes();

            for (Node subflowNode : subflowNodes) {
                if (!subflowNode.getSubflowId().equals(flow.getId()))
                    continue;

                // Is the subflow node attached to any other node or does nobody care if we replace it silently?
                boolean nodeHasWires = subflowDependent.findWiresAttachedToNode(subflowNode).length > 0;

                // Are any of its slot peers no longer in the current flow?
                boolean nodeHasMissingPeers = false;
                if (nodeHasWires) {
                    nodeHasMissingPeers = subflowDependent.findSlotsWithoutPeer(subflowNode, flow).length > 0;
                }

                flowHasWiresAttached = flowHasWiresAttached || nodeHasWires;
                flowHasInvalidPeers = flowHasInvalidPeers || nodeHasMissingPeers;
            }

            dependencyList.add(
                new FlowDependency(subflowDependent.getLabel(), subflowDependent.getIdentifier(), level, flowHasWiresAttached, flowHasInvalidPeers)
            );

            populateSuperDependencies(subflowDependent, level+1, dependencyList);
        }
    }

    protected void populateSubDependencies(Flow flow, boolean hydrate, int level, List<FlowDependency> dependencyList) {

        Set<String> added = new HashSet<>();

        Node[] subflowNodes = flow.findSubflowNodes();

        for (Node subflowNode : subflowNodes) {
            Flow subflow = findFlow(subflowNode.getSubflowId());

            // Do we have a loop?
            if (subflow.getId().equals(flow.getId())) {
                throw new IllegalStateException(
                    "Loop detected, can't have flow as its own subflow: " + flow
                );
            }
            for (FlowDependency superDependency : flow.getSuperDependencies()) {
                if (subflow.getId().equals(superDependency.getId())) {
                    throw new IllegalStateException(
                        "Loop detected in '" + flow + "', subflow is also super dependency: " + subflow
                    );
                }
            }

            if (subflow == null)
                throw new IllegalStateException(
                    "Missing subflow dependency '" + subflowNode.getSubflowId() + "': in " + subflowNode
                );

            if (!added.contains(subflow.getId())) {
                dependencyList.add(new FlowDependency(subflow.getLabel(), subflow.getIdentifier(), hydrate ? subflow : null, level));
                added.add(subflow.getId());
                populateSubDependencies(subflow, hydrate, ++level, dependencyList);
            }

        }
    }

    protected abstract Flow findFlow(String flowId);

    protected abstract Flow[] findSubflowDependents(String flowId);

    protected abstract void stopFlowIfRunning(Flow flow);

    protected abstract void storeFlow(Flow flow);

}