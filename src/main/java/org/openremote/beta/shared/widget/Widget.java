package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Node;

import java.util.Map;

import static org.openremote.beta.shared.util.Util.createMap;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

@JsExport
@JsType
public class Widget {

    public static final String TYPE_TEXT_LABEL = "urn:org-openremote:widget:textlabel";
    public static final String TYPE_PUSH_BUTTON = "urn:org-openremote:widget:pushbutton";

    static public String getWidgetComponent(Map<String, Object> widgetProperties) {
        String type = getString(widgetProperties, "type");
        if (type == null)
            throw new IllegalArgumentException("Missing type property");
        switch(type) {
            case TYPE_TEXT_LABEL:
                return "or-console-widget-textlabel";
            case TYPE_PUSH_BUTTON:
                return "or-console-widget-pushbutton";
            default:
                throw new IllegalArgumentException("No component for: " + type);
        }
    }

    @JsNoExport
    static public void configureProperties(Node node, String type) {
        Map<String, Object> widgetProperties = createMap(getMap(node.getProperties()), "widget");
        widgetProperties.put("type", type);
        switch (type) {
            case TYPE_TEXT_LABEL:
                break;
            case TYPE_PUSH_BUTTON:
                break;
            default:
                throw new IllegalArgumentException("Don't know how to configure: " + type);
        }
    }
}
