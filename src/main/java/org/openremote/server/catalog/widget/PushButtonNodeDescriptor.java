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
import org.openremote.server.catalog.WidgetNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Slot;

import java.util.List;

public class PushButtonNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:openremote:widget:pushbutton";
    public static final String TYPE_LABEL = "Push Button";

    public static final String COMPONENT = "or-console-widget-pushbutton";

    public static final ObjectNode PUSH_BUTTON_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, COMPONENT)
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
        slots.add(new Slot("Click", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, "click"));
        slots.add(new Slot("Text", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "text"));
        slots.add(new Slot("Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "backgroundColor"));
        slots.add(new Slot("Text Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "color"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-node-editor-pushbutton");
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return PUSH_BUTTON_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("text");
        propertyPaths.add("backgroundColor");
        propertyPaths.add("color");
        propertyPaths.add("fontSizePixels");
        propertyPaths.add("icon");
        propertyPaths.add("iconPosition");
        propertyPaths.add("raised");
    }
}
