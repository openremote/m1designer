package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Properties;

import java.util.Map;

@JsExport
@JsType
public class Widget {

    @JsNoExport
    static public Map<String, Object> configureProperties(Node node, String type, String component) {
        Map<String, Object> widgetProperties = Properties.create(node.getProperties(), "widget");
        widgetProperties.put("type", type);
        widgetProperties.put("component", component);
        return Properties.create(widgetProperties, "default");
    }

    public Widget() {

        /*
        addProperty(
            "positionX",
            new PropertyDescriptor.INTEGER("PositionX", "Position offset from left")
                .setRequired(true)
        );
        addProperty(
            "positionY",
            new OldProperty("PositionY", "Position offset from top", OldProperty.Type.NUMBER)
                .setDefaultValue("0")
                .setRequired(true)
        );
        addProperty(
            "positionZ",
            new OldProperty("PositionZ", "Position offset from background", OldProperty.Type.NUMBER)
                .setRequired(true)
        );
        */
    }

}
