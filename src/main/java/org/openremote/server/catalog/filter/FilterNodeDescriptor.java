package org.openremote.server.catalog.filter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.List;

import static org.openremote.server.util.JsonUtil.JSON;

public class FilterNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:openremote:flow:node:filter";
    public static final String TYPE_LABEL = "Filter";

    public static final ObjectNode FILTER_INITIAL_PROPERTIES = JSON.createObjectNode()
        .put("waitForTrigger", false)
        .put("dropEmpty", false)
        .put("dropFalse", false);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return TYPE_LABEL;
    }

    @Override
    public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
        return new FilterRoute(context, flow, node);
    }

    @Override
    public void addSlots(List<Slot> slots) {
        super.addSlots(slots);
        slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
        slots.add(new Slot("Trigger", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
        slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE)));
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-node-editor-filter");
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return FILTER_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("waitForTrigger");
        propertyPaths.add("dropEmpty");
        propertyPaths.add("dropFalse");
    }

}
