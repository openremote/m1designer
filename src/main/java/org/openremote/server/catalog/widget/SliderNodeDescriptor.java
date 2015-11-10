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

public class SliderNodeDescriptor extends WidgetNodeDescriptor {

    public static final String TYPE = "urn:openremote:widget:slider";
    public static final String TYPE_LABEL = "Slider";

    public static final String WIDGET_COMPONENT = "or-console-widget-slider";
    public static final String EDITOR_COMPONENT = "or-node-editor-slider";

    public static final ObjectNode SLIDER_INITIAL_PROPERTIES = WIDGET_INITIAL_PROPERTIES.deepCopy()
        .put(PROPERTY_COMPONENT, WIDGET_COMPONENT)
        .put("minValue", 0)
        .put("maxValue", 99)
        .put("widthPixels", 250)
        .put("color", "#ccc")
        .put("knobColor", "#c1d72f")
        .put("progressColor", "#c1d72f")
        .put("editable", false)
        .put("pin", true)
        .put("pinColor", "#c1d72f")
        .put("pinTextColor", "#455a64")
        .put("editable", false);

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
        slots.add(new Slot("Value", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "value"));
        slots.add(new Slot("Value", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, "value"));
        slots.add(new Slot("Width", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "widthPixels"));
        super.addSlots(slots);
    }

    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add(EDITOR_COMPONENT);
    }

    @Override
    protected ObjectNode getInitialProperties() {
        return SLIDER_INITIAL_PROPERTIES;
    }

    @Override
    protected void addPersistentPropertyPaths(List<String> propertyPaths) {
        super.addPersistentPropertyPaths(propertyPaths);
        propertyPaths.add("minValue");
        propertyPaths.add("maxValue");
        propertyPaths.add("widthPixels");
        propertyPaths.add("color");
        propertyPaths.add("knobColor");
        propertyPaths.add("progressColor");
        propertyPaths.add("editable");
        propertyPaths.add("pin");
        propertyPaths.add("pinColor");
        propertyPaths.add("pinTextColor");
        propertyPaths.add("editable");
    }
}
