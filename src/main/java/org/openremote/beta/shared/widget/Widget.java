package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Properties;

import java.util.Map;

@JsExport
@JsType
public class Widget {

    public static final String WIDGET_PROPERTIES = "widget";
    public static final String PROPERTY_COMPONENT = "component";
    public static final String PROPERTY_DEFAULTS = "defaults";

    public static final String PROPERTY_POSITION_X = "positionX";
    public static final String PROPERTY_POSITION_Y = "positionY";

    public static Map<String, Object> getWidgetProperties(Node node) {
        if (!node.getProperties().containsKey(WIDGET_PROPERTIES))
            Properties.create(node.getProperties(), WIDGET_PROPERTIES);
        return Properties.getProperties(node.getProperties(), WIDGET_PROPERTIES);
    }

    public static Map<String, Object> getWidgetDefaults(Node node) {
        Map<String, Object> widgetProperties = getWidgetProperties(node);
        if (!widgetProperties.containsKey(PROPERTY_DEFAULTS))
            Properties.create(widgetProperties, PROPERTY_DEFAULTS);
        return Properties.getProperties(widgetProperties, PROPERTY_DEFAULTS);
    }
}
