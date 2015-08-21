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

import static org.openremote.beta.client.shared.Timeout.debounce;
import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_COMPONENT;
import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_TYPE_LABEL;

@JsExport
@JsType
public class FlowNodePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodePresenter.class);

    public Flow flow;
    public Node node;
    public boolean flowNodeDirty;
    public String flowNodeTitle = "Node";
    public Slot[] sinks = new Slot[0];
    public boolean hasMultipleSinks;

    public FlowNodePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowNodeEditEvent.class, event -> {
            this.flow = event.getFlow();
            this.notifyPath("flow");

            this.node = event.getNode();
            this.notifyPath("node");

            setFlowNodeDirty(false);
            setFlowNodeTitle();

            if (node.isClientAccessEnabled()) {
                sinks = node.findSlots(Slot.TYPE_SINK);
                notifyPath("sinks", sinks);
                hasMultipleSinks = sinks.length > 1;
                notifyPath("hasMultipleSinks", hasMultipleSinks);
            }

            dispatchEvent(
                new FlowNodeOpenEvent(
                    Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_COMPONENT)
                )
            );
        });
    }

    public String getSinkLabel(Slot sink) {
        return sink.getLabel() != null && sink.getLabel().length() > 0
            ? sink.getLabel() + " Slot"
            : "Sink Slot";
    }

    public void nodePropertyChanged() {
        setFlowNodeDirty(true);
        setFlowNodeTitle();
        debounce("NodePropertyChange", () -> {
            if (flowNodeDirty && flow != null && node != null) {
                dispatchEvent(new NodeUpdatedEvent(flow, node));
                setFlowNodeDirty(false);
            }
        }, 500);
    }

    public void sendSinkMessage(Slot sink, String body) {
        String instanceId = null;

        if (node.isOfType(Node.TYPE_SUBFLOW)) {
            instanceId = flow.getId();
        }

        MessageEvent messageEvent = new MessageEvent(sink, instanceId, body);
        dispatchEvent(new MessageSendEvent(messageEvent));
    }

    protected void setFlowNodeDirty(boolean dirty) {
        flowNodeDirty = dirty;
        notifyPath("flowNodeDirty", flowNodeDirty);
    }

    protected void setFlowNodeTitle() {
        if (node != null) {
            flowNodeTitle = node.getLabel() != null
                ? node.getLabel() + " (" + Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL) + ")"
                : Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL);
        } else {
            flowNodeTitle = "No node selected";
        }
        notifyPath("flowNodeTitle", flowNodeTitle);
    }
}
