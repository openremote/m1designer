package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

@JsExport
@JsType
public class ConsolePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsolePresenter.class);

    public boolean haveWidgets;

    public ConsolePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(MessageSendEvent.class);

        addEventListener(ConsoleRefreshEvent.class, event -> {
            Element container = clearContainer();
            updateWidgets(event.getFlow(), container);
        });

        addEventListener(MessageReceivedEvent.class, event -> {

            String instanceId = event.getMessageEvent().getInstanceId();
            String sinkId = event.getMessageEvent().getSinkSlotId();

            String sinkSelector = "or-console-widget-sink" +
                "[slot='" + sinkId + "']" +
                "[instance='" + (instanceId != null ? instanceId : "") + "']";
            NodeList sinks = getContainer().querySelectorAll(sinkSelector);
            for (int i = 0; i < sinks.getLength(); i++) {
                Element sink = (Element) sinks.item(i);
                dispatchEvent(sink, event);
            }
        });
    }

    protected Element getContainer() {
        return getRequiredChildView("#container");
    }

    protected Element clearContainer() {
        Element container = getContainer();
        Element child;
        while (((child = container.getFirstElementChild()) != null)) {
            container.removeChild(child);
        }
        haveWidgets = false;
        return container;
    }

    protected void updateWidgets(Flow flow, Element container) {
        updateWidgets(flow, flow, container, null);
    }

    protected void updateWidgets(Flow rootFlow, Flow currentFlow, Element container, String instanceId) {
        addWidgets(currentFlow, container, instanceId);

        Node[] subflowNodes = currentFlow.findNodes(Node.TYPE_SUBFLOW);

        for (Node subflowNode : subflowNodes) {

            Flow subflow = rootFlow.findSubflow(subflowNode);
            if (subflow == null) {
                LOG.warn("Illegal subflow node, can't find referenced peer: " + subflowNode);
                continue;
            }

            Element compositeWidget = addWidget(subflowNode, container);

            updateWidgets(rootFlow, subflow, compositeWidget, subflowNode.getId());
        }
    }

    protected void addWidgets(Flow flow, Element container, String instanceId) {
        Node[] clientNodes = flow.findNodes(Node.TYPE_CLIENT);
        haveWidgets = haveWidgets || clientNodes.length > 0;
        for (Node node : clientNodes) {
            Element widget = addWidget(node, container);
            addWidgetSinks(node, widget, instanceId);
        }
    }

    protected Element addWidget(Node node, Element container) {
        Map<String, Object> widgetProperties = getMap(getMap(node.getProperties()), "widget");
        if (widgetProperties == null)
            return null;

        LOG.debug("Adding widget: " + widgetProperties);

        String widgetComponent = getString(widgetProperties, "component");

        Element widget = container.getOwnerDocument().createElement(widgetComponent);

        Map<String, Object> widgetDefaults = getMap(widgetProperties, "default");
        for (Map.Entry<String, Object> entry : widgetDefaults.entrySet()) {
            widget.setAttribute(Util.toLowerCaseDash(entry.getKey()), entry.getValue().toString());
        }

        container.appendChild(widget);

        return widget;
    }

    protected void addWidgetSinks(Node node, Element widget, String instanceId) {
        Slot[] sinkSlots = node.findSlots(Slot.TYPE_SINK);
        for (Slot sink : sinkSlots) {
            Element widgetSink = widget.getOwnerDocument().createElement("or-console-widget-sink");
            widgetSink.setAttribute("slot", sink.getId());
            widgetSink.setAttribute("instance", instanceId != null ? instanceId: "");
            widget.appendChild(widgetSink);
        }
    }

}
