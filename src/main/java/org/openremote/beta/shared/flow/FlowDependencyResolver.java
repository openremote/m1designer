package org.openremote.beta.shared.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowDependencyResolver {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependencyResolver.class);

    public void populateDependencies(Flow flow) {
        populateFlowDependencies(flow);
    }

    protected void populateFlowDependencies(Flow flow) {
        flow.clearDependencies();

        Node[] subflowNodes = flow.findSubflowNodes();

        for (Node subflowNode : subflowNodes) {
            Flow subflow = findFlow(subflowNode.getSubflowId());

            if (subflow == null)
                throw new IllegalStateException(
                    "Missing subflow dependency '" + subflowNode.getSubflowId() + "': in " + subflowNode
                );

            flow.addDependency(subflow);

            populateFlowDependencies(subflow);
        }
    }

    protected abstract Flow findFlow(String slotId);

}