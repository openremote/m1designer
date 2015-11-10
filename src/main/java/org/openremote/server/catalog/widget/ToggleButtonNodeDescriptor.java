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

public class ToggleButtonNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:openremote:widget:toggelbutton";
    public static final String TYPE_LABEL = "Toggle Button";

    public static final String WIDGET_COMPONENT = "or-console-widget-togglebutton";
    public static final String EDITOR_COMPONENT = "or-node-editor-togglebutton";

    public static final ObjectNode TOGGLE_BUTTON_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("onIcon", "check-box")
        .put("offIcon", "check-box-outline-blank")
        .put("iconSizePixels", 40)
        .put("onColor", "#c1d72f")
        .put("offColor", "#cccccc")
        .put("iconBackgroundColor", "transparent");

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
        slots.add(new Slot("Checked", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "checked"));
        slots.add(new Slot("Checked", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, "checked"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return TOGGLE_BUTTON_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("onIcon");
        propertyPaths.add("offIcon");
        propertyPaths.add("iconSizePixels");
        propertyPaths.add("onColor");
        propertyPaths.add("offColor");
        propertyPaths.add("iconBackgroundColor");
    }
}
