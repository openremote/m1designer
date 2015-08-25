package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.Component.DOM;
import org.openremote.beta.client.shared.JsUtil;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.openremote.beta.client.shared.Timeout.debounce;

@JsExport
@JsType
public class FlowNodePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodePresenter.class);

    public Flow flow;
    public Node node;
    public boolean isSubflow;
    public boolean flowNodeDirty;
    public String flowNodeTitle = "Node";
    public Slot[] sinks = new Slot[0];
    public boolean hasMultipleSinks;

    public FlowNodePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowNodeEditEvent.class, event -> {
            flow = event.getFlow();
            notifyPath("flow");

            node = event.getNode();
            notifyPath("node");

            isSubflow = node.isOfType(Node.TYPE_SUBFLOW);
            notifyPath("isSubflow", isSubflow);

            createEditorComponents(node.getEditorSettings().getComponents());

            setSinkSlots();

            dispatchEvent(new FlowNodeOpenEvent());

            // Undo the dirty state by the notifyPath above, so we don't fire node update event
            setFlowNodeDirty(false);
            setFlowNodeTitle();
        });

        addEventListener(FlowNodeCloseEvent.class, event -> {
            this.flow = null;
            this.notifyPath("flow");
            this.node = null;
            this.notifyPath("node");
            setFlowNodeDirty(false);
            setFlowNodeTitle();
            removeEditorComponents();
        });

        addEventListener(NodePropertiesRefreshEvent.class, event -> {
            if (node != null && node.getId().equals(event.getNodeId())) {
                updateEditorComponents();
            }
        });

        addEventListener(NodePropertiesUpdatedEvent.class, true, event -> {
            if (node != null && node.getId().equals(event.getNodeId())) {
                node.setProperties(event.getNodeProperties());
                nodeChanged();
            }
        });
    }

    public void deleteNode() {
        if (flow == null || node == null)
            return;
        flow.removeNode(node);
        dispatchEvent(new NodeDeletedEvent(flow, node));
        dispatchEvent(new FlowNodeCloseEvent());
    }

    public String getSinkLabel(Slot sink) {
        return sink.getLabel() != null && sink.getLabel().length() > 0
            ? sink.getLabel() + " Slot"
            : FlowDesignerConstants.SLOT_SINK_LABEL + " Slot";
    }

    public void nodeChanged() {
        // This is complicated: We set everything to dirty and expect to fire an update to
        // everyone. But we give it a chance to become non-dirty of a few milliseconds, then
        // we don't fire. The purpose here is to tame the eager Polymer change observers and
        // to avoid firing too many updates when users edit text.
        setFlowNodeDirty(true);
        setFlowNodeTitle();
        debounce("NodeChange", () -> {
            if (flowNodeDirty && flow != null && node != null) {
                dispatchEvent(new NodeUpdatedEvent(flow, node));
                setSinkSlots();
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

    protected DOM getEditorComponentContainer() {
        return getRequiredElementDOM("#editorComponentContainer");
    }

    protected void createEditorComponents(String[] editorComponents) {
        DOM container = getEditorComponentContainer();
        while (container.getLastChild() != null) {
            container.removeChild(container.getLastChild());
        }

        if (editorComponents == null)
            return;

        for (String editorComponent : editorComponents) {
            LOG.debug("Creating and adding editor component: " + editorComponent);
            Component component =
                (Component) getView().getOwnerDocument().createElement(editorComponent);
            component.set("nodeId", node.getId());
            container.appendChild((Element) component);
        }

        updateEditorComponents();
    }

    protected void updateEditorComponents() {
        JavaScriptObject nodeProperties = JavaScriptObject.createObject();
        if (node.getProperties() != null)
            nodeProperties = JsonUtils.safeEval(node.getProperties());

        // TODO: This was stripped out to null if I use the Component.DOM API by the GWT compiler...
        Element container = getRequiredElement("#editorComponentContainer");
        for (int i = 0; i < container.getChildNodes().getLength(); i++) {
            Component component = (Component) container.getChildNodes().item(i);
            component.set("nodeProperties", nodeProperties);
        }
    }

    protected void removeEditorComponents() {
        DOM container = getEditorComponentContainer();
        while (container.getLastChild() != null) {
            container.removeChild(container.getLastChild());
        }
    }

    protected void setSinkSlots() {
        if (node == null)
            return;
        if (node.isClientAccess()) {
            sinks = node.findSlots(Slot.TYPE_SINK);
            notifyPath("sinks", sinks);
            hasMultipleSinks = sinks.length > 1;
            notifyPath("hasMultipleSinks", hasMultipleSinks);
        } else {
            sinks = new Slot[0];
            notifyPath("sinks", sinks);
            hasMultipleSinks = false;
            notifyPath("hasMultipleSinks", hasMultipleSinks);
        }
    }

    protected void setFlowNodeDirty(boolean dirty) {
        flowNodeDirty = dirty;
        notifyPath("flowNodeDirty", flowNodeDirty);
    }

    protected void setFlowNodeTitle() {
        if (node != null) {
            flowNodeTitle = node.getLabel() != null
                ? node.getLabel() + " (" + node.getEditorSettings().getTypeLabel() + ")"
                : node.getEditorSettings().getTypeLabel();
        } else {
            flowNodeTitle = "No node selected";
        }
        notifyPath("flowNodeTitle", flowNodeTitle);
    }
}
