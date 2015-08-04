package org.openremote.beta.server.route.procedure;

import org.apache.camel.CamelContext;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowObject;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class FlowProcedure {

    final protected CamelContext context;
    final protected Flow flow;

    final protected Set<FlowObject> processedFlowObjects = new HashSet<>();

    public FlowProcedure(CamelContext context, Flow flow) {
        this.context = context;
        this.flow = flow;
    }

    public CamelContext getContext() {
        return context;
    }

    public Flow getFlow() {
        return flow;
    }

    public Node[] getUnprocessedNodes() {
        List<Node> list = new ArrayList<>();
        for (Node node : flow.getNodes()) {
            if (!processedFlowObjects.contains(node)) {
                list.add(node);
            }
        }
        return list.toArray(new Node[list.size()]);
    }

    public void clearProcessed() {
        processedFlowObjects.clear();
    }

    public void addProcessed(FlowObject flowObject) {
        processedFlowObjects.add(flowObject);
    }

}
