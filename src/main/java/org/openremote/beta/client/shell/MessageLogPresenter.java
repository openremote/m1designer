package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageLogPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogPresenter.class);

    public Flow flow;

    public MessageLogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowEditEvent.class, true, false, event -> {
            this.flow = event.getFlow();
        });
    }

    public boolean isCurrentFlow(MessageEvent messageEvent) {
        return flow != null && flow.getId().equals(messageEvent.getFlowId());
    }

    public String getFlowLabel(MessageEvent messageEvent) {
        if (isCurrentFlow(messageEvent)) {
            return flow.getLabel();
        }
        return messageEvent.getFlowId();
    }

    public String getNodeLabel(MessageEvent messageEvent) {
        if (isCurrentFlow(messageEvent)) {
            Node node = flow.findNode(messageEvent.getNodeId());
            if (node != null)
                return node.getLabel();
        }
        return messageEvent.getNodeId();
    }

    public String getSinkLabel(MessageEvent messageEvent) {
        if (isCurrentFlow(messageEvent)) {
            Node node = flow.findNode(messageEvent.getNodeId());
            if (node != null) {
                Slot slot = node.findSlot(messageEvent.getSinkSlotId());
                if (slot != null && slot.isOfType(Slot.TYPE_SINK) && slot.getLabel() != null)
                    return slot.getLabel();
            }
        }
        return messageEvent.getSinkSlotId();
    }

    public boolean haveSinkLabel(MessageEvent messageEvent) {
        if (isCurrentFlow(messageEvent)) {
            Node node = flow.findNode(messageEvent.getNodeId());
            if (node != null) {
                Slot slot = node.findSlot(messageEvent.getSinkSlotId());
                if (slot != null && slot.isOfType(Slot.TYPE_SINK) && slot.getLabel() != null)
                    return true;
            }
        }
        return false;
    }
}
