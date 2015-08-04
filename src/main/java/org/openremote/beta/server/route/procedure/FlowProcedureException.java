package org.openremote.beta.server.route.procedure;

import org.openremote.beta.shared.event.FlowManagementPhase;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class FlowProcedureException extends Exception {

    final protected FlowManagementPhase phase;
    final protected Flow flow;
    final protected Node node;
    final Node[] unprocessedNodes;

    public FlowProcedureException(Throwable cause, FlowManagementPhase phase, Flow flow, Node node, Node[] unprocessedNodes) {
        super(cause);
        this.phase = phase;
        this.flow = flow;
        this.node = node;
        this.unprocessedNodes = unprocessedNodes;
    }

    public FlowManagementPhase getPhase() {
        return phase;
    }

    public Flow getFlow() {
        return flow;
    }

    public Node getNode() {
        return node;
    }

    public Node[] getUnprocessedNodes() {
        return unprocessedNodes;
    }
}
