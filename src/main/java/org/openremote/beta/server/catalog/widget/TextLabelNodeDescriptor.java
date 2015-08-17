package org.openremote.beta.server.catalog.widget;

import org.openremote.beta.server.catalog.WidgetNodeDescriptor;
import org.openremote.beta.shared.widget.TextLabel;

public class TextLabelNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:textlabel";
    public static final String TYPE_LABEL = "Text Label";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return TYPE_LABEL;
    }

    @Override
    protected String getComponent() {
        return TextLabel.COMPONENT;
    }
}
