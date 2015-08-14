package org.openremote.beta.shared.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowDependencyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependencyResolver.class);

    public void populateDependencies(Flow flow) {

        flow.clearDependencies();

        String[] subflowSlotIds= flow.findWiredSubflowSlotIds();

        for (String subflowSlotId : subflowSlotIds) {
            Flow subflow = findOwnerFlowOfSlot(subflowSlotId);

            if (subflow == null)
                throw new IllegalStateException(
                    "Missing subflow dependency slot '" + subflowSlotId + "': in " + flow
                );

            flow.addDependency(subflow);

            populateDependencies(subflow);
        }
    }

    protected abstract Flow findOwnerFlowOfSlot(String slotId);

}