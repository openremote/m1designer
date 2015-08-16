package org.openremote.beta.shared.widget;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.PropertyDescriptor;

@JsExport
@JsType
public class PushButton extends Widget {

    public static final String TYPE = "urn:org-openremote:widget:pushbutton";
    public static final String COMPONENT = "or-console-widget-pushbutton";

    public PushButton() {
/*
        addProperty("text",
            new PropertyDescriptor.STRING("Text", "Button Text")
                .setRequired(true)
        );
*/
    }
}
