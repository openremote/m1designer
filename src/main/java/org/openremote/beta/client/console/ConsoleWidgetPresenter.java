package org.openremote.beta.client.console;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.json.client.JSONObject;
import elemental.dom.NodeList;
import elemental.js.util.JsArrayOfString;
import org.openremote.beta.client.event.ConsoleWidgetModifiedEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.shared.session.event.ServerSendEvent;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsExport
@JsType
public class ConsoleWidgetPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetPresenter.class);

    public static final String VISITED_WIDGETS = "VISITED_WIDGETS";

    public ConsoleWidgetPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void widgetPropertiesChanged(JavaScriptObject jso, String path, Object value) {
        LOG.debug("Change on widget '" + getView().getLocalName() + "' path '" + path + "': " + value);

        String[] persistentPaths = (String[]) getViewComponent().get("persistentPropertyPaths");
        if (persistentPaths == null)
            persistentPaths = new String[0];
        boolean persistentPathChange = Arrays.asList(persistentPaths).contains(path);

        String nodeId = (String) getViewComponent().get("nodeId");

        if (persistentPathChange) {
            LOG.debug("Persistent path changed, dispatching widget node update event: " + path);
            // This updates the editor's flow and node state
            String widgetProperties = new JSONObject(jso).toString();
            dispatch(new ConsoleWidgetModifiedEvent(nodeId, widgetProperties));
        }

        // Find a child source slot with a property-path that matches the changed path
        String sourceSelector = "or-console-widget-slot[type='" + Slot.TYPE_SOURCE + "'][property-path='" + path + "']";
        NodeList sourceNodes = getView().querySelectorAll(sourceSelector);
        LOG.debug("Found source slots matching property path: " + sourceNodes.getLength());

        // Now send a message to these slots
        for (int i = 0; i < sourceNodes.getLength(); i++) {
            Component slotComponent = (Component) sourceNodes.item(i);
            String slotId = (String) slotComponent.get("slotId");
            String instanceId = (String) slotComponent.get("instanceId");

            // Add this widget to the list of visited nodes
            JsArrayOfString visitedWidgetsArray = (JsArrayOfString)getViewComponent().get("visitedWidgets");

            // Null the flag so it's gone after this JS event loop
            getViewComponent().set("visitedWidgets", JsArrayOfString.create());

            // Preserve existing path
            List<String> visitedWidgets = new ArrayList<>();
            if (visitedWidgetsArray != null && visitedWidgetsArray.length() > 0) {
                for (int j = 0; j < visitedWidgetsArray.length(); j++) {
                    visitedWidgets.add(visitedWidgetsArray.get(j));
                }
            }

            // Add myself
            visitedWidgets.add(nodeId);

            // Send it along with the message as a comma separated string
            Map<String, Object> headers = new HashMap<>();
            headers.put(VISITED_WIDGETS, Joiner.on(",").join(visitedWidgets));
            LOG.debug("Setting visited widgets header: " + headers.get(VISITED_WIDGETS));

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