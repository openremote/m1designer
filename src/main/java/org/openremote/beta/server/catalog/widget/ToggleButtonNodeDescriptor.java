package org.openremote.beta.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.WidgetNodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class ToggleButtonNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:toggelbutton";
    public static final String TYPE_LABEL = "Toggle Button";

    public static final String WIDGET_COMPONENT = "or-console-widget-togglebutton";
    public static final String EDITOR_COMPONENT = "or-editor-node-togglebutton";

    public static final ObjectNode TOGGLE_BUTTON_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("onIcon", "check-box")
        .put("offIcon", "check-box-outline-blank")
        .put("iconSizePixels", 40)
        .put("onColor", "#c1d72f")
        .put("offColor", "#cccccc")
        .put("iconBackgroundColor", "transparent");

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return TYPE_LABEL;
    }

    @Override
    public void addSlots(List<Slot> slots) {
        slots.add(new Slot("Checked", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "checked"));
        slots.add(new Slot("Checked", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), "checked"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return TOGGLE_BUTTON_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("onIcon");
        propertyPaths.add("offIcon");
        propertyPaths.add("iconSizePixels");
        propertyPaths.add("onColor");
        propertyPaths.add("offColor");
        propertyPaths.add("iconBackgroundColor");
    }
}
