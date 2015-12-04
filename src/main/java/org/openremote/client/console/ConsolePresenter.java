/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.console;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.js.util.JsMapFromStringTo;
import elemental.js.util.JsMapFromStringToString;
import jsinterop.annotations.JsType;
import org.openremote.client.shared.*;
import org.openremote.shared.event.FlowRuntimeFailureEvent;
import org.openremote.shared.event.client.*;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowDependency;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: This approach of mixing light/shadow DOM manipulation only works with shady DOM, not real Shadow DOM!
 */
@JsType
public class ConsolePresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsolePresenter.class);

    public static final int SWITCH_LONG_PRESS_DELAY_MILLIS = 1500;

    public boolean shellOpened = false;
    public boolean editMode = false;
    public double zoomFactor = 1.0;

    protected Flow flow;

    public ConsolePresenter(View view) {
        super(view);

        addListener(ConsoleRefreshEvent.class, event -> {
            getView().fire(event.getType(), event);
            flow = event.getFlow();
            notifyPath("flow");
            refreshConsole(event.getSelectedNodeId());
        });

        addListener(ConsoleRefreshedEvent.class, event-> {
            getView().fire(event.getType(), event);
        });

        addListener(ConsoleEditModeEvent.class, event -> {
            editMode = event.isEditMode();
            notifyPath("editMode", editMode);
        });

        addListener(ConsoleZoomEvent.class, event -> {
            zoomFactor = event.getZoomFactor();
            notifyPath("zoomFactor", zoomFactor);
        });

        addListener(NodeSelectedEvent.class, event -> {
            selectWidget(event.getNodeId());
        });

        addListener(ShellCloseEvent.class, event-> {
            if (shellOpened) {
                shellOpened = false;
                notifyPath("shellOpened", shellOpened);
            }
        });

        addListener(ConsoleLoopDetectedEvent.class, event -> {
            LOG.warn("Exchange stopped, loop detected. Message has already been processed by: " + event.getNodeId());
            if (flow != null) {
                dispatch(new FlowRuntimeFailureEvent(
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
        new LongPressListener(getView(), SWITCH_LONG_PRESS_DELAY_MILLIS, this::toggleShell);
    }

    public void toggleShell() {
        shellOpened = !shellOpened;
        notifyPath("shellOpened", shellOpened);
        if (shellOpened) {
            dispatch(new ShellOpenEvent(flow));
        } else {
            dispatch(new ShellCloseEvent());
        }
    }

    public void widgetSelected(String nodeId) {
        dispatch(new NodeSelectedEvent(nodeId));
    }

    public void onDrop(String label, String nodeType,
                       String subflowId,
                       String sensorEndpoint, String discoveryEndpoint, String actuatorEndpoint,
                       double positionX, double positionY) {
        // TODO ugly
        JsMapFromStringToString nodeProperties = JsMapFromStringToString.create();
        if (sensorEndpoint != null && sensorEndpoint.length() > 0) {
            nodeType = "urn:openremote:flow:node:sensor";
            nodeProperties.put("sensorEndpoint", sensorEndpoint);
            if (discoveryEndpoint != null && discoveryEndpoint.length() > 0) {
                nodeProperties.put("discoveryEndpoint", discoveryEndpoint);
            }
        } else if (actuatorEndpoint != null && actuatorEndpoint.length() > 0) {
            nodeType = "urn:openremote:flow:node:actuator";
            nodeProperties.put("actuatorEndpoint", actuatorEndpoint);
        }

        if (nodeType != null && nodeType.length() > 0) {
            dispatch(new NodeCreateEvent(
                flow,
                label,
                nodeType,
                JsonUtils.stringify(nodeProperties),
                positionX,
                positionY,
                true
            ));
        } else if (subflowId != null && subflowId.length() > 0) {
            dispatch(new SubflowNodeCreateEvent(flow, subflowId, positionX, positionY, true));
        }
    }

    protected void selectWidget(String nodeId) {
        // TODO: Weird CSS query hacks necessary because Component DOM API doesn't work properly
        NodeList widgetNodes = JsUtil.querySelectorAll(getView(), "#widgetComponentContainer > .consoleWidget");
        for (int i = 0; i < widgetNodes.getLength(); i++) {
            View widgetView = (View) widgetNodes.item(i);
            String widgetNodeId = (String) widgetView.get("nodeId");
            widgetView.toggleClass("selected", nodeId.equals(widgetNodeId), widgetView);
        }
    }

    protected void refreshConsole(String selectedNodeId) {
        DOM container = getDOM((View) getWidgetComponentContainer());
        clearWidgetContainer(container);
        if (flow != null) {
            updateWidgets(flow, container);
        }

        if (selectedNodeId != null)
            selectWidget(selectedNodeId);

        // TODO: Weird CSS query hacks necessary because Component DOM API doesn't work properly
        NodeList nonCompositeWidgets = JsUtil.querySelectorAll(getView(), "#widgetComponentContainer :not(or-console-widget-composite)");
        dispatch(new ConsoleRefreshedEvent(nonCompositeWidgets.length() > 0));
    }

    protected Element getWidgetComponentContainer() {
        return JsUtil.asElementalElement(getRequiredElement("#widgetComponentContainer"));
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
            DOM compositeDOM = getDOMRoot((View)compositeWidgetNode);
            elemental.dom.Node[] nonCompositeChildren =
                compositeDOM.querySelectorAll(":not(or-console-widget-composite)");
            if (nonCompositeChildren.length == 0) {
                getDOM((View) compositeWidgetNode).getParentNode().removeChild(compositeWidgetNode);
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
        View widget = JsUtil.createView(getView(), widgetComponent);

        widget.toggleClass("consoleWidget", true, (elemental.dom.Node)widget);
        widget.set("nodeId", node.getId());
        widget.set("nodeLabel", node.getDefaultedLabel());
        widget.set("persistentPropertyPaths", node.getPersistentPropertyPaths());

        if (widget.get("onWidgetPropertiesChanged") != null) {
            widget.set("widgetProperties", widgetProperties);
        }

        container.appendChild(JsUtil.asElementalElement(widget));

        // Continue manipulating the local DOM of the widget!
        DOM widgetDOM = getDOMRoot(widget);

        for (Slot slot : node.findPropertySlots()) {
            // If this is a source slot without any wires attached, we don't need to add it
            if (slot.isOfType(Slot.TYPE_SOURCE) && flow.findWiresAttachedToSlot(slot.getId()).length == 0)
                continue;
            widgetDOM.appendChild(createWidgetSlot(slot, instanceId));
        }

        return widgetDOM;
    }

    protected Element createWidgetSlot(Slot slot, String instanceId) {
        View view = JsUtil.createView(getView(), "or-console-widget-slot");
        if (!slot.isLabelEmpty())
            view.set("label", slot.getLabel());
        view.set("type", slot.getType());
        view.set("slotId", slot.getId());
        if (instanceId != null)
            view.set("instanceId", instanceId);
        view.set("propertyPath", slot.getPropertyPath());
        return JsUtil.asElementalElement(view);
    }
}
