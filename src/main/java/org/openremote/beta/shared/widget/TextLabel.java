package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;

@JsExport
@JsType
public class TextLabel extends Widget {

    public static final String TYPE = "urn:org-openremote:widget:textlabel";
    public static final String COMPONENT = "or-console-widget-textlabel";

    public TextLabel() {
/*
        addProperty("text",
            new OldProperty("Text", "Label Text", OldProperty.Type.STRING)
                .setRequired(true)
        );
        addProperty(
            "color",
            new OldProperty("Color", "Text Color", OldProperty.Type.STRING)
                .setDefaultValue("#ff0000")
        );

*/
    }
}
