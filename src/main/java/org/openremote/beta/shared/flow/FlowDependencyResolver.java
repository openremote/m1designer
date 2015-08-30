package org.openremote.beta.shared.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class FlowDependencyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependencyResolver.class);

    public void populateSuperDependencies(Flow flow) {
        List<FlowDependency> dependencyList = new ArrayList<>();
        populateSuperDependencies(flow, 0, dependencyList);
        Collections.reverse(dependencyList);
        flow.setSuperDependencies(dependencyList.toArray(new FlowDependency[dependencyList.size()]));
    }

    public void populateSubDependencies(Flow flow, boolean hydrate) {
        List<FlowDependency> dependencyList = new ArrayList<>();
        populateSubDependencies(flow, hydrate, 0, dependencyList);
        flow.setSubDependencies(dependencyList.toArray(new FlowDependency[dependencyList.size()]));
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
                boolean nodeHasWires = subflowDependent.findWiredNodesOf(subflowNode).length > 0;

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

}