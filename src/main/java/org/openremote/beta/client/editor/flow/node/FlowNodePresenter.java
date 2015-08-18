package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_COMPONENT;
import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_TYPE_LABEL;

@JsExport
@JsType
public class FlowNodePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodePresenter.class);

    public Flow flow;
    public Node node;

    public FlowNodePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowNodeEditEvent.class, event -> {
            this.flow = event.getFlow();
            this.node = event.getNode();
            dispatchEvent(
                new FlowNodeOpenEvent(
                    flow,
                    node,
                    getNodeLabel(node),
                    Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_COMPONENT)
                )
            );
        });
    }

    public Slot[] getAccessibleSinks() {
        if (!node.isClientAccessEnabled())
            return new Slot[0];
        return node.findSlots(Slot.TYPE_SINK);
    }

    public String getNodeLabel(Node node) {
        return node.getLabel() + " (" + Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL) + ")";
    }

    public void nodePropertyChanged() {
        if (flow != null && node != null)
            dispatchEvent(new NodeUpdatedEvent(flow, node));
    }

    public void sendMessage(Slot slot, String body) {
        String instanceId = null;

        if (node.isOfType(Node.TYPE_SUBFLOW)) {
            instanceId = flow.getId();
        }

        MessageEvent messageEvent = new MessageEvent(slot, instanceId, body);
        dispatchEvent(new MessageSendEvent(messageEvent));
    }

}
