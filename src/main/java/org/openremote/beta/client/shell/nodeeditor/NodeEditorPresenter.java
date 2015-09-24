package org.openremote.beta.client.shell.nodeeditor;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.event.*;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shell.floweditor.FlowDesignerConstants;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.client.shared.Timeout.debounce;

@JsExport
@JsType
public class NodeEditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(NodeEditorPresenter.class);

    public Flow flow;
    public Node node;
    public boolean isSubflow;
    public boolean flowNodeDirty;
    public String flowNodeTitle = "Node";
    public Slot[] sinks = new Slot[0];
    public boolean hasMultipleSinks;

    public NodeEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addListener(NodeEditEvent.class, event -> {
            this.flow = event.getFlow();
            notifyPath("flow");

            this.node = event.getNode();
            notifyPath("node");

            isSubflow = node.isOfTypeSubflow();
            notifyPath("isSubflow", isSubflow);

            createEditorComponents(node.getEditorSettings().getComponents());

            setSinkSlots();

            // Undo the dirty state by the notifyPath above, so we don't fire node update event
            setFlowNodeDirty(false);
            setFlowNodeTitle();

        });

        addListener(NodeDeletedEvent.class, event -> {
            closeEditor();
        });

        addListener(NodePropertiesRefreshEvent.class, event -> {
            if (node != null && node.getId().equals(event.getNodeId())) {
                updateEditorComponents();
            }
        });

        addListener(NodePropertiesModifiedEvent.class, event -> {
            if (node != null && node.getId().equals(event.getNodeId())) {
                node.setProperties(event.getNodeProperties());
                nodeChanged();
            }
        });

        addListener(FlowEditEvent.class, event -> closeEditor());
        addListener(FlowDeletedEvent.class, event -> closeEditor());

    }

    public void editSubflow() {
        if (!isSubflow)
            return;
        dispatch(new FlowLoadEvent(node.getSubflowId()));
    }

    public void duplicateNode() {
        dispatch(new NodeDuplicateEvent(flow, node));
    }

    public void deleteNode() {
        dispatch(new NodeDeleteEvent(flow, node));
    }

    public String getSinkLabel(Slot sink) {
        return !sink.isLabelEmpty()
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
                dispatch(new FlowModifiedEvent(flow, true));
                dispatch(new NodeModifiedEvent(flow, node));
                setSinkSlots();
                setFlowNodeDirty(false);
            }
        }, 500);
    }

    public void sendSinkMessage(Slot sink, String body) {
        String instanceId = null;

        if (node.isOfTypeSubflow()) {
            instanceId = flow.getId();
        }

        Message message = new Message(sink, instanceId, body);
        dispatch(new MessageSendEvent(message));
    }

    protected void closeEditor() {
        this.flow = null;
        notifyPathNull("flow");
        this.node = null;
        notifyPathNull("node");
        setFlowNodeDirty(false);
        setFlowNodeTitle();
        removeEditorComponents();
    }

    protected Component.DOM getEditorComponentContainer() {
        return getRequiredElementDOM("#editorComponentContainer");
    }

    protected void createEditorComponents(String[] editorComponents) {
        Component.DOM container = removeEditorComponents();

        if (editorComponents == null)
            return;

        for (String editorComponent : editorComponents) {
            LOG.debug("Creating and adding editor component: " + editorComponent);
            Component component =
                (Component) getView().getOwnerDocument().createElement(editorComponent);
            // TODO: error handling, how do we detect a missing editor component?
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

    protected Component.DOM removeEditorComponents() {
        Component.DOM container = getEditorComponentContainer();
        while (container.getLastChild() != null) {
            container.removeChild(container.getLastChild());
        }
        return container;
    }

    protected void setSinkSlots() {
        if (node.isClientAccess()) {
            sinks = node.findNonPropertySlots(Slot.TYPE_SINK);
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
            flowNodeTitle = !node.isLabelEmpty()
                ? node.getLabel()
                : node.getEditorSettings().getTypeLabel();
        } else {
            flowNodeTitle = "No node selected";
        }
        notifyPath("flowNodeTitle", flowNodeTitle);
    }
}
