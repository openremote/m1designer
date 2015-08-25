package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;

@JsType
public class MessageLogDetail {

    public String flowLabel;
    public String nodeLabel;
    public String sinkLabel;
    public String instanceLabel;
    public String body;

    public MessageLogDetail(MessageEvent event, Flow flow, Node node, Slot slot) {
        this.flowLabel = flow != null ? flow.getLabel() : null;

        if (node != null) {
            this.nodeLabel = node.getLabel() != null
                ? node.getLabel() + " (" + node.getEditorSettings().getTypeLabel() + ")"
                : node.getEditorSettings().getTypeLabel();
        }

        if (node == null || slot == null || !node.getLabel().equals(slot.getLabel())) {
            this.sinkLabel = slot != null ? slot.getLabel() : event.getSinkSlotId();
        }
        this.instanceLabel = event.getInstanceId();
        this.body = event.getBody() != null && event.getBody().length() > 0 ? event.getBody() : "(EMPTY MESSAGE)";
    }

}
