package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class MessageEvent {

    public String flowId;
    public String nodeId;
    public String sinkSlotId;
    public String instanceId;
    public Object headers;
    public String body;

    public MessageEvent() {
    }

    public MessageEvent(Flow flow, Node node, Slot sinkSlot, String body) {
        this(flow, node, sinkSlot, null, body, null);
    }

    public MessageEvent(Flow flow, Node node, Slot sinkSlot, String instanceId, String body) {
        this(flow, node, sinkSlot, instanceId, body, null);
    }

    public MessageEvent(Flow flow, Node node, Slot sinkSlot, String instanceId, String body, Object headers) {
        this(flow.getIdentifier().getId(), node.getIdentifier().getId(), sinkSlot.getIdentifier().getId(), instanceId, body, headers);
    }

    public MessageEvent(String flowId, String nodeId, String sinkSlotId) {
        this(flowId, nodeId, sinkSlotId, null, null, null);
    }

    public MessageEvent(String flowId, String nodeId, String sinkSlotId, String instanceId) {
        this(flowId, nodeId, sinkSlotId, instanceId, null, null);
    }

    public MessageEvent(String flowId, String nodeId, String sinkSlotId, String instanceId, String body, Object headers) {
        this.flowId = flowId;
        this.nodeId = nodeId;
        this.sinkSlotId = sinkSlotId;
        this.instanceId = instanceId;
        this.body = body;
        this.headers = headers;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSinkSlotId() {
        return sinkSlotId;
    }

    public void setSinkSlotId(String sinkSlotId) {
        this.sinkSlotId = sinkSlotId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Object getHeaders() {
        return headers;
    }

    public void setHeaders(Object headers) {
        this.headers = headers;
    }

    public boolean hasHeaders() {
        return getHeaders() != null;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
            " flowId='" + flowId+ '\'' +
            ", nodeId='" + nodeId + '\'' +
            ", sinkSlotId='" + sinkSlotId + '\'' +
            ", instanceId='" + instanceId + '\'' +
            ", headers=" + headers +
            ", body='" + body + '\'' +
            '}';
    }
}
