package org.openremote.beta.client.console;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.json.client.JSONObject;
import elemental.dom.NodeList;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@JsExport
@JsType
public class ConsoleWidgetPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetPresenter.class);

    public ConsoleWidgetPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void widgetPropertiesChanged(String nodeId, JavaScriptObject jso, String path, Object value) {
        LOG.debug("Change on widget '" + getView().getLocalName() + "' path '" + path + "': " + value);

        String[] persistentPaths = (String[]) getViewComponent().get("persistentPropertyPaths");
        if (persistentPaths == null)
            persistentPaths = new String[0];
        boolean persistentPathChange = Arrays.asList(persistentPaths).contains(path);

        if (persistentPathChange) {
            LOG.debug("Persistent path changed, dispatching widget node update event: " + path);
            // This updates the editor's flow and node state
            String widgetProperties = new JSONObject(jso).toString();
            dispatchEvent(new ConsoleWidgetUpdatedEvent(nodeId, widgetProperties));
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
            MessageEvent messageEvent = new MessageEvent(
                slotId,
                instanceId,
                value != null ? value.toString() : null,
                null
            );
            dispatchEvent(new ConsoleMessageSendEvent(messageEvent));
        }
    }

}