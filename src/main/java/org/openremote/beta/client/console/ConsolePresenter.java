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
import org.openremote.beta.shared.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openremote.beta.shared.util.Util.getMap;

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
            updateWidgets(container, event.getFlow());
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

    protected void updateWidgets(Element container, Flow flow) {
        updateWidgets(container, flow, flow, null);
    }

    protected void updateWidgets(Element container, Flow rootFlow, Flow currentFlow, String subflowInstanceId) {
        Node[] clientNodes = currentFlow.findNodes(Node.TYPE_CLIENT);
        haveWidgets = haveWidgets || clientNodes.length > 0;
        for (Node node : clientNodes) {
            Map<String, Object> widgetProperties = getMap(getMap(node.getProperties()), "widget");
            if (widgetProperties == null)
                continue;

            String widgetComponent = Widget.getWidgetComponent(widgetProperties);

            Element widget = container.getOwnerDocument().createElement(widgetComponent);
            container.appendChild(widget);

            Slot[] sinkSlots = node.findSlots(Slot.TYPE_SINK);
            for (Slot sink : sinkSlots) {
                Element widgetSink = container.getOwnerDocument().createElement("or-console-widget-sink");
                widgetSink.setAttribute("slot", sink.getId());
                widgetSink.setAttribute("instance", subflowInstanceId != null ? subflowInstanceId : "");
                widget.appendChild(widgetSink);
            }
        }

        Node[] subflowNodes = currentFlow.findNodes(Node.TYPE_SUBFLOW);
        for (Node subflowNode : subflowNodes) {

            Slot[] slots = subflowNode.getSlots();
            List<String> handledSubflows = new ArrayList<>();

            for (Slot slot : slots) {
                String peerSlotId = slot.getPeerIdentifier().getId();
                if (peerSlotId == null)
                    continue;
                Flow subflow = rootFlow.findOwnerFlowOfSlot(peerSlotId);
                if (subflow != null) {
                    if (handledSubflows.contains(subflow.getId())) {
                        continue;
                    }
                    updateWidgets(container, rootFlow, subflow, subflowNode.getId());
                    handledSubflows.add(subflow.getId());
                }
            }
        }
    }

}
