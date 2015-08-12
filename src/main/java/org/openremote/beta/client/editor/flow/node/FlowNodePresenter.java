package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

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
            dispatchEvent(new FlowNodeOpenEvent(event.getFlow(), event.getNode(), getNodeLabel(event.getNode())));
        });
    }

    protected boolean isClientAccessEnabled(Node node) {
        // Should we accept client message events for this node (its sinks) and should
        // we send client message events when this node is done processing (its sources)
        return (node.hasProperties() && Boolean.valueOf(getString(getMap(node.getProperties()), "clientAccess")))
            || node.isOfType(Node.TYPE_CLIENT); // TODO ugly, fix with better node type system
    }

    public Slot[] getAccessibleSinks() {
        if (!isClientAccessEnabled(node)) // Ugly
            return new Slot[0];
        return node.findSlots(Slot.TYPE_SINK);
    }

    public String getNodeLabel(Node node) {
        return node.getLabel() + " (" + Node.getTypeLabel(node.getIdentifier().getType()) + ")";
    }

    public void sendMessage(Slot slot, String body) {
        String instanceId = null;

        // TODO Ugh
        if (node.isOfType(Node.TYPE_SUBFLOW)) {
            instanceId = flow.getId();
        }

        MessageEvent messageEvent = new MessageEvent(
            flow, node, slot, instanceId, body
        );
        dispatchEvent(new MessageSendEvent(messageEvent));
    }

}
