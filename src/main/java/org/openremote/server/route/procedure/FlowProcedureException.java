package org.openremote.server.route.procedure;

import org.openremote.shared.event.FlowDeploymentPhase;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

public class FlowProcedureException extends Exception {

    final protected FlowDeploymentPhase phase;
    final protected Flow flow;
    final protected Node node;
    final Node[] unprocessedNodes;

    public FlowProcedureException(Throwable cause, FlowDeploymentPhase phase, Flow flow, Node node, Node[] unprocessedNodes) {
        super(cause);
        this.phase = phase;
        this.flow = flow;
        this.node = node;
        this.unprocessedNodes = unprocessedNodes;
    }

    public FlowDeploymentPhase getPhase() {
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
