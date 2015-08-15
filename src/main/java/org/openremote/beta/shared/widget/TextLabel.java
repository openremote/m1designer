package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Property;

@JsExport
@JsType
public class TextLabel extends Widget {

    public static final String TYPE = "urn:org-openremote:widget:textlabel";
    public static final String COMPONENT = "or-console-widget-textlabel";

    public TextLabel() {
        addProperty("text",
            new Property("Text", "Label Text", Property.Type.STRING)
                .setRequired(true)
        );
        addProperty(
            "color",
            new Property("Color", "Text Color", Property.Type.STRING)
                .setDefaultValue("#ff0000")
        );

    }
}
