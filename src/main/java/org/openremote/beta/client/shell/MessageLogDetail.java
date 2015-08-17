package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Properties;

import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_TYPE_LABEL;

@JsType
public class MessageLogDetail {

    public String flowLabel;
    public String nodeLabel;
    public String sinkLabel;
    public String instanceLabel;
    public String body;

    public MessageLogDetail(MessageEvent event, Flow msgFlow, Node msgNode, Slot msgSlot) {
        this.flowLabel = msgFlow != null ? msgFlow.getLabel() : null;
        this.nodeLabel = msgNode != null ? msgNode.getLabel() + " (" + Properties.get(msgNode.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL) + ")" : null;
        if (msgNode == null || msgSlot == null || !msgNode.getLabel().equals(msgSlot.getLabel())) {
            this.sinkLabel = msgSlot != null ? msgSlot.getLabel() : event.getSinkSlotId();
        }
        this.instanceLabel = event.getInstanceId();
        this.body = event.getBody() != null && event.getBody().length() > 0 ? event.getBody() : "(EMPTY MESSAGE)";
    }

}
