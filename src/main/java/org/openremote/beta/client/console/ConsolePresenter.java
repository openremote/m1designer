package org.openremote.beta.client.console;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.js.util.JsMapFromStringTo;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.Component.DOM;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
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

    public ConsolePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(ConsoleReadyEvent.class);
        addRedirectToShellView(MessageSendEvent.class);
        addRedirectToShellView(ConsoleWidgetUpdatedEvent.class);

        addEventListener(ConsoleRefreshEvent.class, event -> {
            DOM container = getDOM(getWidgetComponentContainer());
            clearWidgetContainer(container);
            if (event.getFlow() != null) {
                updateWidgets(event.getFlow(), container);
            }
            dispatchEvent(new ConsoleRefreshedEvent());
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            LOG.debug("Message event received from server: " + event.getMessageEvent());
            messageReceived(event.getMessageEvent());
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent(new ConsoleReadyEvent());
    }

    protected Element getWidgetComponentContainer() {
        return getRequiredElement("#widgetComponentContainer");
    }

    protected void messageReceived(MessageEvent event) {
        String instanceId = event.getInstanceId();
        String sinkId = event.getSinkSlotId();

        String sinkSelector = "or-console-widget-sink[slot='" + sinkId + "']";
        if (instanceId != null) {
            sinkSelector += "[instance='" + instanceId + "']";
        }

        LOG.debug("Received message, querying sinks: " + sinkSelector);
        // USE THE LIGHT DOM FOR THIS QUERY! This only works with shady DOM!
        NodeList sinks = getWidgetComponentContainer().querySelectorAll(sinkSelector);
        LOG.debug("Found sinks: " + sinks.getLength());
        for (int i = 0; i < sinks.getLength(); i++) {
            Element sink = (Element) sinks.item(i);
            dispatchEvent(sink, event);
        }
    }

    protected void clearWidgetContainer(DOM container) {
        while (container.getLastChild() != null) {
            container.removeChild(container.getLastChild());
        }
    }

    protected void updateWidgets(Flow flow, DOM container) {
        updateWidgets(flow, flow, container, null);
    }

    protected void updateWidgets(Flow rootFlow, Flow currentFlow, DOM container, String instanceId) {
        LOG.debug("Updating widgets of flow: " + currentFlow);
        addWidgets(currentFlow, container, instanceId);

        Node[] subflowNodes = currentFlow.findNodes(Node.TYPE_SUBFLOW);

        for (Node subflowNode : subflowNodes) {

            Flow subflow = rootFlow.findSubflow(subflowNode);
            if (subflow == null) {
                LOG.warn("Illegal subflow node, can't find referenced peer: " + subflowNode);
                continue;
            }

            DOM compositeWidget = addWidget(subflowNode, container);
            updateWidgets(rootFlow, subflow, compositeWidget, subflowNode.getId());
        }
    }

    protected void addWidgets(Flow flow, DOM container, String instanceId) {
        Node[] widgetNodes = flow.findClientWidgetNodes();
        LOG.debug("Adding widgets '" + flow + "': " + widgetNodes.length);
        for (Node node : widgetNodes) {

            if (node.isOfType(Node.TYPE_SUBFLOW))
                continue;

            DOM widget = addWidget(node, container);
            addWidgetSinks(node, widget, instanceId);
        }
    }

    protected DOM addWidget(Node node, DOM container) {
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

        String widgetComponent = (String) ((JsMapFromStringTo)widgetProperties).get("component");
        if (widgetComponent == null) {
            LOG.debug("Widget node has no widget component property: " + node.getProperties());
            return container;
        }

        LOG.debug("Creating widget component: " + widgetComponent);
        Component widget = (Component)getView().getOwnerDocument().createElement(widgetComponent);

        widget.set("nodeId", node.getId());

        if (widget.get("onWidgetPropertiesChanged") != null) {
            // Must therefore be PropertiesAwareWidget
            widget.set("widgetProperties", widgetProperties);
        }

        container.appendChild((Element)widget);

        // Continue manipulating the local DOM of the widget!
        return getDOMRoot((Element)widget);
    }

    protected void addWidgetSinks(Node node, DOM widget, String instanceId) {
        Slot[] sinkSlots = node.findSlots(Slot.TYPE_SINK);
        for (Slot sink : sinkSlots) {
            Element widgetSink = getView().getOwnerDocument().createElement("or-console-widget-sink");
            widgetSink.setAttribute("slot", sink.getId());
            widgetSink.setAttribute("instance", instanceId != null ? instanceId : "");
            widget.appendChild(widgetSink);
        }
    }

}
