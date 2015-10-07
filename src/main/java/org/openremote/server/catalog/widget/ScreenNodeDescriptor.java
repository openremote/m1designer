package org.openremote.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.catalog.ClientNodeDescriptor;
import org.openremote.server.catalog.WidgetNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.server.util.JsonUtil;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.Arrays;
import java.util.List;

import static org.openremote.server.util.JsonUtil.JSON;

public class ScreenNodeDescriptor extends ClientNodeDescriptor {

    public static final String TYPE = "urn:openremote:widget:screen";
    public static final String TYPE_LABEL = "Screen";

    public static final String WIDGET_COMPONENT = "or-console-widget-screen";
    public static final String EDITOR_COMPONENT = "or-node-editor-screen";

    public static final ObjectNode SCREEN_INITIAL_PROPERTIES = JSON.createObjectNode()
        .put(WidgetNodeDescriptor.PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("backgroundColor", "#aaa")
        .put("textColor", "white");

    @Override
    public Node initialize(Node node) {
        node = super.initialize(node);
        node.setClientWidget(true);
        return node;
    }

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
        slots.add(new Slot("Background Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "backgroundColor"));
        slots.add(new Slot("Text Color", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), "textColor"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return SCREEN_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add(WidgetNodeDescriptor.PROPERTY_COMPONENT);
        propertyPaths.add("backgroundColor");
        propertyPaths.add("textColor");
    }
}
