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

package org.openremote.client.shell.floweditor;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Shadow;
import com.ait.lienzo.shared.core.types.ColorName;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.client.shared.Timeout.debounce;
import static org.openremote.client.shell.floweditor.FlowDesignerConstants.*;

public class SlotShape {

    private static final Logger LOG = LoggerFactory.getLogger(SlotShape.class);

    public static final double SLOT_DIMENSIONS = (SLOT_RADIUS + SLOT_PADDING) * 2;

    public class Handle extends Circle {

        final protected SlotShape slotShape;

        public Handle(SlotShape slotShape) {
            super(SLOT_RADIUS - SLOT_PADDING);
            this.slotShape = slotShape;
        }

        public SlotShape getSlotShape() {
            return slotShape;
        }
    }

    protected NodeShape nodeShape;
    final protected Slot slot;
    final protected boolean isSource;
    final protected Handle handle;
    final protected Text slotLabelText;
    final protected Text slotValueText;

    public SlotShape(NodeShape nodeShape, Slot slot) {
        this.nodeShape = nodeShape;
        this.slot = slot;
        this.isSource = slot.isOfType(Slot.TYPE_SOURCE);

        this.handle = new Handle(this);
        handle.setFillColor(isSource ? SLOT_SOURCE_COLOR : SLOT_SINK_COLOR);
        nodeShape.add(handle);

        this.slotLabelText = new Text("", FONT_FAMILY, SLOT_FONT_SIZE);
        slotLabelText.setFontStyle("normal");
        slotLabelText.setFillColor(isSource ? SLOT_SOURCE_TEXT_COLOR : SLOT_SINK_TEXT_COLOR);
        slotLabelText.setListening(false);
        slotLabelText.setVisible(false);
        nodeShape.add(slotLabelText);

        this.slotValueText = new Text("", FONT_FAMILY, SLOT_VALUE_FONT_SIZE);
        slotValueText.setFontStyle("normal");
        slotValueText.setFillColor(SLOT_VALUE_TEXT_COLOR);
        slotValueText.setVisible(false);
        nodeShape.add(slotValueText);

        handle.addNodeMouseDownHandler(event -> createWire());
        handle.addNodeTouchStartHandler(event -> createWire());
    }

    public NodeShape getNodeShape() {
        return nodeShape;
    }

    public boolean isSource() {
        return isSource;
    }

    public Slot getSlot() {
        return slot;
    }

    public Shape getHandle() {
        return handle;
    }

    public double getX() {
        return getNodeShape().getX() + getHandle().getX();
    }

    public double getY() {
        return getNodeShape().getY() + getHandle().getY();
    }

    public void updateNodeShape(NodeShape nodeShape, double x, double y) {
        this.nodeShape = nodeShape;

        nodeShape.add(handle);
        handle.setX(x - (SLOT_PADDING));
        handle.setY(isSource ? y + (SLOT_RADIUS + SLOT_PADDING) : y - (SLOT_RADIUS + SLOT_PADDING));

        nodeShape.add(slotLabelText);
        nodeShape.add(slotValueText);

        updateSlotText();
    }

    public void setAttached(boolean attached) {
        if (attached) {
            getHandle().setFillColor(getSlot().isOfType(Slot.TYPE_SINK) ? SLOT_SINK_ATTACHED_COLOR : SLOT_SOURCE_ATTACHED_COLOR);
        } else {
            getHandle().setFillColor(getSlot().isOfType(Slot.TYPE_SINK) ? SLOT_SINK_COLOR : SLOT_SOURCE_COLOR);
        }
    }

    public void setSlotValue(String value) {
        if (value == null || value.length() == 0) {
            value = "EMPTY";
            slotValueText.setFontStyle("italic");
        } else {
            slotValueText.setFontStyle("normal");
        }
        if (value.length() > SLOT_VALUE_MAX_LENGTH) {
            value = value.substring(0, SLOT_VALUE_MAX_LENGTH - 3) + "...";
        }
        slotValueText.setText(value);

        slotValueText.setShadow(new Shadow(SLOT_VALUE_TEXT_HIGHLIGHT_SHADOW_COLOR, 5, 0, 0));
        slotValueText.setFillColor(SLOT_VALUE_TEXT_HIGHLIGHT_COLOR);
        getHandle().setStrokeColor(SLOT_HANDLE_HIGHLIGHT_OUTLINE_COLOR);
        getHandle().setStrokeWidth(SLOT_PADDING);

        if (slot.isOfType(Slot.TYPE_SOURCE)) {
            WireShape[] wireShapes = getNodeShape().getAttachedWireShapes(slot.getId());
            for (WireShape wireShape : wireShapes) {
                wireShape.setPulse(true);
            }
        }

        debounce(
            "HighlightSlot" + getSlot().getId(),
            () -> {
                slotValueText.setShadow(null);
                slotValueText.setFillColor(SLOT_VALUE_TEXT_COLOR);
                getNodeShape().getLayer().batch();
                getHandle().setStrokeColor(ColorName.TRANSPARENT);
                getHandle().setStrokeWidth(0);

                if (slot.isOfType(Slot.TYPE_SOURCE)) {
                    WireShape[] wireShapes = getNodeShape().getAttachedWireShapes(slot.getId());
                    for (WireShape wireShape : wireShapes) {
                        wireShape.setPulse(false);
                    }
                }
            },
            250
        );

        updateSlotText();
    }

    protected void updateSlotText() {
        boolean haveSlotLabel = !slot.isLabelEmpty();
        boolean haveSlotValue = slotValueText.getText().length() > 0;

        String slotLabel = haveSlotLabel ? slot.getLabel() : (isSource ? SLOT_SOURCE_LABEL : SLOT_SINK_LABEL);

        slotLabelText.setText(slotLabel);
        slotLabelText.setVisible(true);
        double slotLabelWidth = slotLabelText.getBoundingBox().getWidth();
        double slotLabelHeight = slotLabelText.getBoundingBox().getHeight();
        slotLabelText.setX(isSource ? handle.getX() - slotLabelWidth - SLOT_DIMENSIONS / 2 : handle.getX() + SLOT_DIMENSIONS / 2 + SLOT_PADDING * 2);
        if (haveSlotValue) {
            slotLabelText.setY(handle.getY() - 2);
        } else {
            slotLabelText.setY(handle.getY() + slotLabelHeight / 2);
        }

        if (haveSlotValue) {
            slotValueText.setVisible(true);
            double slotValueWidth = slotValueText.getBoundingBox().getWidth();
            double slotValueHeight = slotValueText.getBoundingBox().getHeight();
            slotValueText.setX(isSource ? handle.getX() - slotValueWidth - SLOT_DIMENSIONS / 2 : handle.getX() + SLOT_DIMENSIONS / 2 + SLOT_PADDING * 2);
            slotValueText.setY(handle.getY() + slotValueHeight);
        }
    }

    protected void createWire() {
        WireShape wireShape;
        if (getSlot().isOfType(Slot.TYPE_SINK)) {
            wireShape = getNodeShape().createWireShape(getX(), getY(), getX(), getY(), null, getSlot());
            wireShape.getSinkHandle().setAttachedSlotShape(this);
            wireShape.getSourceHandle().moveToTop();
        } else {
            wireShape = getNodeShape().createWireShape(getX(), getY(), getX(), getY(), getSlot(), null);
            wireShape.getSourceHandle().setAttachedSlotShape(this);
            wireShape.getSinkHandle().moveToTop();
        }
    }

    @Override
    public String toString() {
        return "SlotShape{" +
            "slot=" + slot +
            '}';
    }
}
