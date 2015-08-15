package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Property;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.openremote.beta.shared.util.Util.createMap;
import static org.openremote.beta.shared.util.Util.getMap;

@JsExport
@JsType
public class Widget {

    @JsNoExport
    static public Map<String, Object> configureProperties(Node node, String type, String component) {
        Map<String, Object> widget = createMap(getMap(node.getProperties()), "widget");
        widget.put("type", type);
        widget.put("component", component);
        return createMap(widget, "default");
    }

    public Map<String, Property> properties = new LinkedHashMap<>();

    public Widget() {
        addProperty(
            "positionX",
            new Property("PositionX", "Position offset from left", Property.Type.NUMBER)
                .setRequired(true)
        );
        addProperty(
            "positionY",
            new Property("PositionY", "Position offset from top", Property.Type.NUMBER)
                .setDefaultValue("0")
                .setRequired(true)
        );
        addProperty(
            "positionZ",
            new Property("PositionZ", "Position offset from background", Property.Type.NUMBER)
                .setRequired(true)
        );
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void addProperty(String name, Property property) {
        properties.put(name, property);
    }

}
