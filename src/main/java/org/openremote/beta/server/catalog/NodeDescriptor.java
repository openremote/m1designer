package org.openremote.beta.server.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.beta.server.util.JsonUtil.JSON;

public abstract class NodeDescriptor {

    abstract public String getType();

    abstract public String getTypeLabel();

    abstract public NodeRoute createRoute(CamelContext context, Flow flow, Node node);

    public boolean isInternal() {
        return false;
    }

    public NodeColor getColor() {
        return NodeColor.DEFAULT;
    }

    public Node createNode() {
        return initialize(
            new Node(null, new Identifier(IdentifierUtil.generateGlobalUniqueId(), getType()))
        );
    }

    public Node initialize(Node node) {

        List<Slot> slots = new ArrayList<>();
        addSlots(slots);
        node.setSlots(slots.toArray(new Slot[slots.size()]));

        node.getEditorSettings().setTypeLabel(getTypeLabel());
        node.getEditorSettings().setNodeColor(getColor());

        List<String> editorComponents = new ArrayList<>();
        addEditorComponents(editorComponents);
        node.getEditorSettings().setComponents(editorComponents.toArray(new String[editorComponents.size()]));

        Object initialProperties = getInitialProperties();
        try {
            if (initialProperties != null) {
                node.setProperties(JSON.writeValueAsString(initialProperties));
            } else {
                ObjectNode properties = JSON.createObjectNode();
                configureInitialProperties(properties);
                if (properties.size() > 0) {
                    node.setProperties(JSON.writeValueAsString(properties));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error writing initial properties of: " + getType(), ex);
        }

        List<String> persistentPaths = new ArrayList<>();
        addPersistentPropertyPaths(persistentPaths);
        if (persistentPaths.size() > 0) {
            node.setPersistentPropertyPaths(persistentPaths.toArray(new String[persistentPaths.size()]));
        }

        return node;
    }

    public void addSlots(List<Slot> slots) {
        // Subclass
    }

    public void addEditorComponents(List<String> editorComponents) {
        // Subclass
    }

    protected void configureInitialProperties(ObjectNode properties) {
        // Subclass
    }

    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        // Subclass
    }

    protected ObjectNode getInitialProperties() {
        return null;
    }
}
