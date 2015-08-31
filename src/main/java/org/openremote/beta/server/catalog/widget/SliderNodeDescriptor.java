package org.openremote.beta.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.WidgetNodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class SliderNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:slider";
    public static final String TYPE_LABEL = "Slider";

    public static final String WIDGET_COMPONENT = "or-console-widget-slider";
    public static final String EDITOR_COMPONENT = "or-editor-node-slider";

    public static final ObjectNode SLIDER_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("minValue", 0)
        .put("maxValue", 99)
        .put("widthPixels", 250)
        .put("color", "#ccc")
        .put("knobColor", "#c1d72f")
        .put("progressColor", "#c1d72f")
        .put("editable", false)
        .put("pin", true)
        .put("pinColor", "#c1d72f")
        .put("pinTextColor", "#455a64")
        .put("editable", false);

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
        slots.add(new Slot("Value", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "value"));
        slots.add(new Slot("Value", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), "value"));
        slots.add(new Slot("Width", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "widthPixels"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return SLIDER_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("minValue");
        propertyPaths.add("maxValue");
        propertyPaths.add("widthPixels");
        propertyPaths.add("color");
        propertyPaths.add("knobColor");
        propertyPaths.add("progressColor");
        propertyPaths.add("editable");
        propertyPaths.add("pin");
        propertyPaths.add("pinColor");
        propertyPaths.add("pinTextColor");
        propertyPaths.add("editable");
    }
}
