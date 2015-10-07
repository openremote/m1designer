package org.openremote.server.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.Arrays;
import java.util.List;

import static org.openremote.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.server.util.JsonUtil.JSON;

/**
 * TODO Milestone 2: I should be able to deploy this as a JSON file + some HTML files to create a widget
 */
public abstract class WidgetNodeDescriptor extends ClientNodeDescriptor {

    public static final String PROPERTY_COMPONENT = "component";
    public static final String WIDGET_EDITOR_COMPONENT = "or-node-editor-widget";

    public static final ObjectNode WIDGET_INITIAL_PROPERTIES = JSON.createObjectNode()
        .put("positionX", 25)
        .put("positionY", 25)
        .put("positionZ", 0)
        .put("opacity", 1.0);

    public static final String[] WIDGET_PERSISTENT_PROPERTY_PATHS = new String[]{
        "component",
        "positionX",
        "positionY",
        "positionZ",
        "opacity"
    };

    @Override
    public Node initialize(Node node) {
        node = super.initialize(node);
        node.setClientWidget(true);
        return node;
    }

    @Override
    public void addSlots(List<Slot> slots) {
        super.addSlots(slots);
        slots.add(new Slot("Position X", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), "positionX"));
        slots.add(new Slot("Position Y", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), "positionY"));
        slots.add(new Slot("Position Z", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), "positionZ"));
        slots.add(new Slot("Opacity", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), "opacity"));
        slots.add(new Slot("Position X", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), "positionX"));
        slots.add(new Slot("Position Y", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), "positionY"));
        slots.add(new Slot("Position Z", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), "positionZ"));
        slots.add(new Slot("Opacity", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), "opacity"));
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(WIDGET_EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return WIDGET_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.addAll(Arrays.asList(WIDGET_PERSISTENT_PROPERTY_PATHS));
    }
}
