package org.openremote.beta.client.console;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.js.util.JsMapFromStringTo;
import org.openremote.beta.client.shell.event.ConfirmationEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.Component.DOM;
import org.openremote.beta.client.shared.LongPressListener;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.FlowRuntimeFailureEvent;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependency;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: This approach of mixing light/shadow DOM manipulation only works with shady DOM, not real Shadow DOM!
 */
@JsExport
@JsType
public class ConsolePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsolePresenter.class);

    public static final int SWITCH_LONG_PRESS_DELAY_MILLIS = 1500;

    public boolean maximized = true;
    public boolean editMode = false;
    public double zoomFactor = 1.0;

    protected Flow flow;
    protected boolean flowDirty;

    public ConsolePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(ConfirmationEvent.class);
        addRedirectToShellView(ConsoleReadyEvent.class);
        addRedirectToShellView(ConsoleSwitchEvent.class);
        addRedirectToShellView(ConsoleRefreshedEvent.class);
        addRedirectToShellView(ConsoleMessageSendEvent.class);
        addRedirectToShellView(ConsoleWidgetUpdatedEvent.class);
        addRedirectToShellView(ConsoleWidgetSelectedEvent.class);
        addRedirectToShellView(FlowRuntimeFailureEvent.class);

        addEventListener(ConsoleRefreshEvent.class, event -> {
            flow = event.getFlow();
            flowDirty = event.isDirty();
            refreshConsole(event.getSelectedNodeId());
        });

        addEventListener(ConsoleEditModeEvent.class, event -> {
            editMode = event.isEditMode();
            notifyPath("editMode", editMode);
        });

        addEventListener(ConsoleZoomEvent.class, event -> {
            zoomFactor = event.getZoomFactor();
            notifyPath("zoomFactor", zoomFactor);
        });

        addEventListener(ConsoleWidgetSelectEvent.class, event -> {
            selectWidget(event.getNodeId());
        });

        addEventListener(ConsoleMaximizeEvent.class, event-> {
            switchConsole();
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            LOG.debug("Message received from server: " + event.getMessage());
            onMessage(event.getMessage());
        });

        addEventListener(ConsoleLoopDetectedEvent.class, event -> {
            LOG.warn("Exchange stopped, loop detected. Message has already been processed by: " + event.getNodeId());
            if (flow != null) {
                dispatchEvent(new FlowRuntimeFailureEvent(
                        flow.getId(),
                        "Exchange stopped, loop detected. Message has already been processed by: " + event.getNodeLabel(),
                        event.getNodeId()
                    )
                );
            }
        });
    }

    @Override
    public void ready() {
        super.ready();
        new LongPressListener(getView(), SWITCH_LONG_PRESS_DELAY_MILLIS, this::switchConsole);
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent(new ConsoleReadyEvent());
    }

    public void switchConsole() {
        if (!maximized && flowDirty) {
            dispatchEvent(new ConfirmationEvent(
                "Unsaved Changes",
                "You have edited the current flow and not redeployed/saved the changes. Close?",
                () -> {
                    maximized = !maximized;
                    notifyPath("maximized", maximized);
                    dispatchEvent(new ConsoleSwitchEvent(flow, maximized));
                }
            ));
        } else {
            maximized = !maximized;
            notifyPath("maximized", maximized);
            dispatchEvent(new ConsoleSwitchEvent(flow, maximized));
        }
    }

    public void widgetSelected(String nodeId) {
        dispatchEvent(new ConsoleWidgetSelectedEvent(nodeId));
        selectWidget(nodeId);
    }

    protected void selectWidget(String nodeId) {
        // TODO: Weird CSS query hacks necessary because Component DOM API doesn't work properly
        NodeList widgetNodes = getView().querySelectorAll("#widgetComponentContainer > .consoleWidget");
        for (int i = 0; i < widgetNodes.getLength(); i++) {
            Component widgetComponent = (Component) widgetNodes.item(i);
            String widgetNodeId = (String) widgetComponent.get("nodeId");
            widgetComponent.toggleClass("selected", nodeId.equals(widgetNodeId), (elemental.dom.Node)widgetComponent);
        }
    }

    protected void refreshConsole(String selectedNodeId) {
        DOM container = getDOM(getWidgetComponentContainer());
        clearWidgetContainer(container);
        if (flow != null) {
            updateWidgets(flow, container);
        }

        if (selectedNodeId != null)
            selectWidget(selectedNodeId);

        // TODO: Weird CSS query hacks necessary because Component DOM API doesn't work properly
        NodeList nonCompositeWidgets = getView().querySelectorAll("#widgetComponentContainer :not(or-console-widget-composite)");
        dispatchEvent(new ConsoleRefreshedEvent(nonCompositeWidgets.length() > 0));
    }

    protected Element getWidgetComponentContainer() {
        return getRequiredElement("#widgetComponentContainer");
    }

    protected void onMessage(Message message) {
        String instanceId = message.getInstanceId();
        String slotId = message.getSlotId();

        String sinkSelector = "or-console-widget-slot[type='" + Slot.TYPE_SINK + "'][slot-id='" + slotId + "']";
        if (instanceId != null) {
            sinkSelector += "[instance-id='" + instanceId + "']";
        }

        LOG.debug("Received message, querying sink slots: " + sinkSelector);
        // USE THE LIGHT DOM FOR THIS QUERY! This only works with shady DOM!
        NodeList sinkNodes = getWidgetComponentContainer().querySelectorAll(sinkSelector);
        LOG.debug("Found sink slots: " + sinkNodes.getLength());
        for (int i = 0; i < sinkNodes.getLength(); i++) {
            Element slotElement = (Element) sinkNodes.item(i);
            dispatchEvent(slotElement, message);
        }
    }

    protected void clearWidgetContainer(DOM container) {
        while (container.getLastChild() != null) {
            container.removeChild(container.getLastChild());
        }
    }

    protected void updateWidgets(Flow flow, DOM container) {
        updateWidgets(flow, flow, container, null);

        // Remove all composite widget trees that have only other (empty) composite widget children
        elemental.dom.Node[] compositeWidgets = container.querySelectorAll("or-console-widget-composite");
        for (elemental.dom.Node compositeWidgetNode : compositeWidgets) {
            DOM compositeDOM = getDOMRoot((Element) compositeWidgetNode);
            elemental.dom.Node[] nonCompositeChildren =
                compositeDOM.querySelectorAll(":not(or-console-widget-composite)");
            if (nonCompositeChildren.length == 0) {
                getDOM(compositeWidgetNode).getParentNode().removeChild(compositeWidgetNode);
            }
        }
    }

    protected void updateWidgets(Flow rootFlow, Flow currentFlow, DOM container, String instanceId) {
        LOG.debug("Updating widgets of flow using instance '" + instanceId + "': " + currentFlow);
        addWidgets(currentFlow, container, instanceId);

        Node[] subflowNodes = currentFlow.findSubflowNodes();

        for (Node subflowNode : subflowNodes) {

            FlowDependency subflowDependency = rootFlow.findSubDependency(subflowNode);
            if (subflowDependency == null || subflowDependency.getFlow() == null) {
                LOG.warn("Illegal subflow node, can't find hydrated sub-dependency: " + subflowNode);
                continue;
            }
            Flow subflow = subflowDependency.getFlow();

            DOM compositeWidget = addWidget(currentFlow, subflowNode, container, instanceId);
            updateWidgets(rootFlow, subflow, compositeWidget, instanceId != null ? instanceId : subflowNode.getId());
        }
    }

    protected void addWidgets(Flow flow, DOM container, String instanceId) {
        Node[] widgetNodes = flow.findClientWidgetNodes();
        LOG.debug("Adding widgets '" + flow + "': " + widgetNodes.length);
        for (Node node : widgetNodes) {

            if (node.isOfTypeSubflow())
                continue;

            addWidget(flow, node, container, instanceId);
        }
    }

    protected DOM addWidget(Flow flow, Node node, DOM container, String instanceId) {
        LOG.debug("Adding widget: " + node);
        if (node.getProperties() == null) {
            LOG.debug("Node has no properties, skipping...");
            return container;
        }

        JavaScriptObject widgetProperties;
        try {
            widgetProperties = JsonUtils.safeEval(node.getProperties());
        } catch (Exception ex) {
            LOG.warn("Node '" + node + "' has invalid widget properties: " + node.getProperties());
            return container;
        }

        String widgetComponent = (String) ((JsMapFromStringTo) widgetProperties).get("component");
        if (widgetComponent == null) {
            LOG.debug("Widget node has no widget component property: " + node.getProperties());
            return container;
        }

        LOG.debug("Creating widget component: " + widgetComponent);
        Component widget = (Component) getView().getOwnerDocument().createElement(widgetComponent);

        widget.toggleClass("consoleWidget", true, (elemental.dom.Node)widget);
        widget.set("nodeId", node.getId());
        widget.set("nodeLabel", node.getDefaultedLabel());
        widget.set("persistentPropertyPaths", node.getPersistentPropertyPaths());

        if (widget.get("onWidgetPropertiesChanged") != null) {
            widget.set("widgetProperties", widgetProperties);
        }

        container.appendChild((Element) widget);

        // Continue manipulating the local DOM of the widget!
        DOM widgetDOM = getDOMRoot((Element) widget);

        if (!node.isOfTypeSubflow()) {
            for (Slot slot : node.findPropertySlots()) {
                // If this is a source slot without any wires attached, we don't need to add it
                if (slot.isOfType(Slot.TYPE_SOURCE) && flow.findWiresAttachedToSlot(slot.getId()).length == 0)
                    continue;
                widgetDOM.appendChild(createWidgetSlot(slot, instanceId));
            }
        }

        return widgetDOM;
    }

    protected Element createWidgetSlot(Slot slot, String instanceId) {
        Component component = (Component) getView().getOwnerDocument().createElement("or-console-widget-slot");
        if (!slot.isLabelEmpty())
            component.set("label", slot.getLabel());
        component.set("type", slot.getIdentifier().getType());
        component.set("slotId", slot.getId());
        if (instanceId != null)
            component.set("instanceId", instanceId);
        component.set("propertyPath", slot.getPropertyPath());
        return (Element) component;
    }

    protected void addRedirectToShellView(Class<? extends Event> eventClass) {
        Element shellView = Browser.getWindow().getTop().getDocument().querySelector("#shell");
        if (shellView == null) {
            throw new RuntimeException("Missing 'or-shell' view in browser top window document");
        }
        addEventListener(
            getView(), eventClass, event ->
                dispatchEvent(shellView, event)
        );
    }

}
