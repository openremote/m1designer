package org.openremote.beta.shared.flow;

import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowDependencyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependencyResolver.class);

    public void populateDependencies(Flow flow) {
        populateFlowDependencies(flow);
        populatePeerLabels(flow);
    }

    protected void populateFlowDependencies(Flow flow) {
        flow.clearDependencies();

        String[] subflowSlotIds = flow.findWiredSubflowSlotIds();

        for (String subflowSlotId : subflowSlotIds) {
            Flow subflow = findOwnerFlowOfSlot(subflowSlotId);

            if (subflow == null)
                throw new IllegalStateException(
                    "Missing subflow dependency slot '" + subflowSlotId + "': in " + flow
                );

            flow.addDependency(subflow);

            populateFlowDependencies(subflow);
        }
    }

    protected void populatePeerLabels(Flow flow) {
        for (Slot slot : flow.findSlotsWithPeer()) {
            String peerSlotId = slot.getPeerIdentifier().getId();
            Flow ownerFlow = flow.findOwnerFlowOfSlot(peerSlotId);
            Node ownerNode = ownerFlow.findOwnerNode(peerSlotId);
            slot.setLabel(ownerNode.getLabel());
        }
        for (Flow subflow : flow.getDependencies()) {
            populatePeerLabels(subflow);
        }
    }

    protected abstract Flow findOwnerFlowOfSlot(String slotId);

}