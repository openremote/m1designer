package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.SLOT_SINK_LABEL;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.SLOT_SOURCE_LABEL;

@JsType
public class MessageLogDetail {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogDetail.class);

    public boolean incoming;
    public String flowLabel;
    public String nodeLabel;
    public String slotLabel;
    public String instanceLabel;
    public String body;

    public MessageLogDetail(boolean incoming, Message event, Flow flow, Node node, Slot slot) {
        this.incoming = incoming;

        this.flowLabel = flow != null ? flow.getLabel() : null;

        if (node != null) {
            this.nodeLabel = node.getLabel() != null
                ? node.getLabel() + " (" + node.getEditorSettings().getTypeLabel() + ")"
                : node.getEditorSettings().getTypeLabel();
        }

        if (node == null || slot == null || !(node.getLabel() != null && node.getLabel().equals(slot.getLabel()))) {
            if (slot != null) {
                if (slot.getLabel() != null) {
                    this.slotLabel = slot.getLabel() + (slot.isOfType(Slot.TYPE_SINK) ? " (Sink)" : " (Source)");
                } else {
                    this.slotLabel = slot.isOfType(Slot.TYPE_SINK) ? SLOT_SINK_LABEL : SLOT_SOURCE_LABEL;
                }
            } else {
                this.slotLabel = event.getSlotId();
            }
        }
        this.instanceLabel = event.getInstanceId();

        if (flow != null && event.getInstanceId() != null) {
            Node instanceNode = flow.findNode(event.getInstanceId());
            if (instanceNode != null && instanceNode.getLabel() != null) {
                this.instanceLabel = instanceNode.getLabel();
            }
        }

        this.body = event.getBody() != null && event.getBody().length() > 0 ? event.getBody() : "(EMPTY MESSAGE)";
    }

}
