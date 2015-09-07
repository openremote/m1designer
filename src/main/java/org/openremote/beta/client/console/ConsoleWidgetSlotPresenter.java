package org.openremote.beta.client.console;

import com.google.common.base.Splitter;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.js.util.JsArrayOfString;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.beta.client.console.ConsoleWidgetPresenter.VISITED_WIDGETS;
import static org.openremote.beta.client.shared.JsUtil.host;

@JsExport
@JsType
public class ConsoleWidgetSlotPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetSlotPresenter.class);

    public ConsoleWidgetSlotPresenter(com.google.gwt.dom.client.Element gwtView) {
        super(gwtView);

        addEventListener(Message.class, this::onMessage);
    }

    protected void onMessage(Message message) {

        // Get the parent of the slot and set its widgetProperties value (use the host element, not the document fragment)
        Node parentNode = getDOM(getView()).getParentNode();
        Component parentWidget = host(parentNode);

        String nodeId = (String) parentWidget.get("nodeId");
        String nodeLabel = (String) parentWidget.get("nodeLabel");

        if (message.hasHeaders()) {
            LOG.debug("Received visited widgets header: " + message.getHeaders().get(VISITED_WIDGETS));

            // Do we have a correlation path in the message
            List<String> visitedWidgets = new ArrayList<>(
                Splitter.on(",").splitToList((String)message.getHeaders().get(VISITED_WIDGETS))
            );

            if (visitedWidgets.contains(nodeId)) {
                dispatchEvent(new ConsoleLoopDetectedEvent(nodeId, nodeLabel));
            }

            if (visitedWidgets.size() > 0) {
                // Preserve the correlation path for the next outgoing property change message
                JsArrayOfString jsArray = JsArrayOfString.create();
                for (String visitedNode : visitedWidgets) {
                    jsArray.push(visitedNode);
                }
                // Set the flag so it's available later in this JS event loop
                parentWidget.set("visitedWidgets", jsArray);
            }
        }

        String propertyPath = (String) getViewComponent().get("propertyPath");
        if (propertyPath == null || propertyPath.length() == 0) {
            LOG.debug("Slot without property path, don't know how to handle message: " + getView().getOuterHTML());
        }
        String value = message.getBody();
        LOG.debug("Setting widget '" + ((Element) parentWidget).getLocalName() + "' property path '" + propertyPath + "': " + value);
        parentWidget.set("widgetProperties." + propertyPath, value);
    }
}
