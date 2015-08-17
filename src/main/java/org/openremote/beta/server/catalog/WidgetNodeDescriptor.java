package org.openremote.beta.server.catalog;

import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.widget.Widget;

import java.util.Map;

import static org.openremote.beta.shared.widget.Widget.*;

public abstract class WidgetNodeDescriptor extends ClientNodeDescriptor {

    @Override
    public Node initialize(Node node) {
        Node result = super.initialize(node);

        Widget.getWidgetProperties(node).put(PROPERTY_COMPONENT, getComponent());

        initializeDefaults(Widget.getWidgetDefaults(node));

        return result;
    }

    protected void initializeDefaults(Map<String, Object> properties) {
        properties.put(PROPERTY_POSITION_X, 0);
        properties.put(PROPERTY_POSITION_Y, 0);
    }

    protected abstract String getComponent();


}
