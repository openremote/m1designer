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

package org.openremote.server.catalog.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.catalog.ClientNodeDescriptor;
import org.openremote.server.catalog.WidgetNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;

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
    public CatalogCategory getCatalogCategory() {
        return CatalogCategory.WIDGETS;
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
        slots.add(new Slot("Background Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "backgroundColor"));
        slots.add(new Slot("Text Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "textColor"));
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
