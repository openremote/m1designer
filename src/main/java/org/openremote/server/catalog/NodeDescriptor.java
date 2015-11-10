/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.NodeColor;
import org.openremote.shared.flow.Slot;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.server.util.JsonUtil.JSON;

public abstract class NodeDescriptor {

    abstract public String getType();

    abstract public String getTypeLabel();

    abstract public NodeRoute createRoute(CamelContext context, Flow flow, Node node);

    public CatalogCategory getCatalogCategory() {
        return CatalogCategory.PROCESSORS;
    }

    public NodeColor getColor() {
        return NodeColor.DEFAULT;
    }

    public Node createNode() {
        return initialize(
            new Node(null, IdentifierUtil.generateGlobalUniqueId(), getType())
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

    public List<String> getPersistentPropertyPaths() {
        List<String> persistentPaths = new ArrayList<>();
        addPersistentPropertyPaths(persistentPaths);
        return persistentPaths;
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
