package org.openremote.beta.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.WidgetNodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class TextLabelNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:textlabel";
    public static final String TYPE_LABEL = "Text Label";

    public static final String COMPONENT = "or-console-widget-textlabel";

    public static final ObjectNode TEXT_LABEL_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, COMPONENT)
        .put("emptyValue", "(EMPTY LABEL)")
        .put("fontSizePixels", 15)
        .put("textColor", "#ddd");

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
        slots.add(new Slot("Text", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "text"));
        slots.add(new Slot("Text", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), "text"));
        slots.add(new Slot("Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "textColor"));
        slots.add(new Slot("Font Size", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "fontSizePixels"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-editor-node-textlabel");
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return TEXT_LABEL_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("emptyValue");
        propertyPaths.add("fontSizePixels");
        propertyPaths.add("textColor");
    }
}
