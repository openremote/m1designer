package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class FlowManagementFailure extends FlowEvent {

    public FlowManagementPhase phase;
    public String exceptionType;
    public String message;
    public Node node;
    public Node[] unprocessedNodes;

    public FlowManagementFailure() {
    }

    public FlowManagementFailure(String flowId) {
        super(flowId);
    }

    public FlowManagementFailure(Flow flow) {
        super(flow);
    }

    public FlowManagementFailure(Flow flow, FlowManagementPhase phase, String exceptionType, String message, Node node, Node[] unprocessedNodes) {
        super(flow);
        this.phase = phase;
        this.exceptionType = exceptionType;
        this.message = message;
        this.node = node;
        this.unprocessedNodes = unprocessedNodes;
    }

    public FlowManagementPhase getPhase() {
        return phase;
    }

    public void setPhase(FlowManagementPhase phase) {
        this.phase = phase;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node[] getUnprocessedNodes() {
        return unprocessedNodes;
    }
}
