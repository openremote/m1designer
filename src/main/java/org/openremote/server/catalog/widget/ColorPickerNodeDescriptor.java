package org.openremote.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.catalog.WidgetNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.List;

public class ColorPickerNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:openremote:widget:colorpicker";
    public static final String TYPE_LABEL = "Color Picker";

    public static final String WIDGET_COMPONENT = "or-console-widget-colorpicker";
    public static final String EDITOR_COMPONENT = "or-node-editor-colorpicker";

    public static final ObjectNode COLOR_PICKER_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("color", "#aaaaaa");

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
        slots.add(new Slot("Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), "color"));
        slots.add(new Slot("Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "color"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return COLOR_PICKER_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("color");
    }
}
