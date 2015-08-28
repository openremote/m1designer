package org.openremote.beta.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.WidgetNodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class PushButtonNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:pushbutton";
    public static final String TYPE_LABEL = "Push Button";

    public static final String COMPONENT = "or-console-widget-pushbutton";

    public static final ObjectNode PUSH_BUTTON_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, COMPONENT)
        .put("downValue", "1")
        .put("upValue", "0")
        .put("text", "CLICK")
        .put("backgroundColor", "#ddd")
        .put("color", "black")
        .put("fontSizePixels", 15)
        .put("raised", true);

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
        slots.add(new Slot("Click", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), "click"));
        slots.add(new Slot("Text", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "text"));
        slots.add(new Slot("Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "backgroundColor"));
        slots.add(new Slot("Text Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "color"));
        slots.add(new Slot("Font Size", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "fontSize"));
        slots.add(new Slot("Raised", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "raised"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-editor-node-pushbutton");
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return PUSH_BUTTON_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("downValue");
        propertyPaths.add("upValue");
        propertyPaths.add("text");
        propertyPaths.add("backgroundColor");
        propertyPaths.add("color");
        propertyPaths.add("fontSizePixels");
        propertyPaths.add("icon");
        propertyPaths.add("iconPosition");
        propertyPaths.add("raised");
    }
}
