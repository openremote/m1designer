package org.openremote.client.console;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import elemental.dom.NodeList;
import elemental.js.util.JsArrayOfString;
import jsinterop.annotations.JsType;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.JsUtil;
import org.openremote.client.shared.View;
import org.openremote.shared.event.Message;
import org.openremote.shared.event.client.ConsoleWidgetModifiedEvent;
import org.openremote.shared.event.client.MessageSendEvent;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsType
public class ConsoleWidgetPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetPresenter.class);

    public static final String VISITED_WIDGETS = "VISITED_WIDGETS";

    public ConsoleWidgetPresenter(View view) {
        super(view);
    }

    public void widgetPropertiesChanged(JavaScriptObject jso, String path, Object value) {
        LOG.debug("Change on widget '" + getView().getLocalName()+ "' path '" + path + "': " + value);

        String nodeId = (String) getView().get("nodeId");

        // Get the list of visited widgets so we can set it as a header on outgoing messages
        String visitedWidgetsHeader = null;
        JsArrayOfString visitedWidgetsArray = (JsArrayOfString)getView().get("visitedWidgets");
        if (visitedWidgetsArray != null && visitedWidgetsArray.length() > 0) {
            List<String> visitedWidgets = new ArrayList<>();
            for (int j = 0; j < visitedWidgetsArray.length(); j++) {
                visitedWidgets.add(visitedWidgetsArray.get(j));
            }
            visitedWidgetsHeader = Joiner.on(",").join(visitedWidgets);
        }

        String[] persistentPaths = (String[]) getView().get("persistentPropertyPaths");
        if (persistentPaths == null)
            persistentPaths = new String[0];
        boolean persistentPathChange = Arrays.asList(persistentPaths).contains(path);

        // If this is a persistent property change _NOT_ triggered by a message (we would
        // have a visited widgets header) but by direct manipulation of the property value
        // in the editor (e.g. user dragged a widget)...
        if (persistentPathChange && visitedWidgetsHeader == null) {
            LOG.debug("Persistent path changed, dispatching widget node update event: " + path);
            // ... update the editor's flow and node state
            String widgetProperties = new JSONObject(jso).toString();
            dispatch(new ConsoleWidgetModifiedEvent(nodeId, widgetProperties));
        }

        // Find a child source slot with a property-path that matches the changed path
        String sourceSelector = "or-console-widget-slot[type='" + Slot.TYPE_SOURCE + "'][property-path='" + path + "']";
        NodeList sourceNodes = JsUtil.querySelectorAll(getView(), sourceSelector);
        LOG.debug("Found source slots matching property path: " + sourceNodes.getLength());

        // Send a message to slots
        for (int i = 0; i < sourceNodes.getLength(); i++) {
            View slotView = (View) sourceNodes.item(i);
            String slotId = (String) slotView.get("slotId");
            String instanceId = (String) slotView.get("instanceId");

            LOG.debug("Preparing outgoing message for slot: " + slotId);

            Map<String, Object> headers = new HashMap<>();

            if (visitedWidgetsHeader != null) {
                LOG.debug("Setting visited widgets header: " + visitedWidgetsHeader);
                headers.put(VISITED_WIDGETS, visitedWidgetsHeader);
            }

            Message message = new Message(
                slotId,
                instanceId,
                value != null ? value.toString() : null,
                headers
            );
            dispatch(new MessageSendEvent(message));
        }
    }

}