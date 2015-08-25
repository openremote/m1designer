package org.openremote.beta.server.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;

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

    public Slot[] createSlots() {
        return new Slot[0];
    }

    public Node initialize(Node node) {
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

        return node;
    }

    public void addEditorComponents(List<String> editorComponents) {
    }


    protected void configureInitialProperties(ObjectNode properties) {
        // Subclass
    }

    protected Object getInitialProperties() {
        return null;
    }
}
