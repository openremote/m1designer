package org.openremote.server.catalog.function;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;

import java.util.List;

import static org.openremote.server.util.JsonUtil.JSON;

public class FunctionNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:openremote:flow:node:function";
    public static final String TYPE_LABEL = "Function";

    public static final ObjectNode FUNCTION_INITIAL_PROPERTIES  = JSON.createObjectNode()
        .put("javascript", "result = input");

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
        return new FunctionRoute(context, flow, node);
    }

    @Override
    public void addSlots(List<Slot> slots) {
        super.addSlots(slots);
        slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK));
        slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-node-editor-function");
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return FUNCTION_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("javascript");
    }

}
