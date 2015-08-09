package org.openremote.beta.client.flow.messagelog;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import org.openremote.beta.client.message.MessageServerConnectEvent;
import org.openremote.beta.client.message.MessageSessionPresenter;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageLogPresenter extends MessageSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogPresenter.class);

    public Flow flow;

    public MessageLogPresenter(Element view) {
        super(view);

        addEventListener(FlowMessageLogStartEvent.class, event -> {
            this.flow = event.getFlow();
            dispatchEvent(new MessageServerConnectEvent());
        });
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

    public boolean isCurrentFlow(MessageEvent messageEvent) {
        return flow != null && flow.getId().equals(messageEvent.getFlowId());
    }

}
