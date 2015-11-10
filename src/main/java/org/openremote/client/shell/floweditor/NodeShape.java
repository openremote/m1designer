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

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Shadow;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.DragMode;
import com.ait.lienzo.shared.core.types.IColor;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class NodeShape extends Group {

    private static final Logger LOG = LoggerFactory.getLogger(NodeShape.class);

    protected Node node;
    protected Map<String, SlotShape> slotShapes = new HashMap<>();
    protected MultiPath outline;
    protected MultiPath header;
    protected Text patchLabel;
    protected Text patchTypeLabel;
    protected boolean selected;

    public NodeShape(Node node) {

        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);
        addNodeDragMoveHandler(event -> {
            if (this.node != null) {
                this.node.getEditorSettings().setPositionX(getX());
                this.node.getEditorSettings().setPositionY(getY());
            }
        });
        addNodeDragEndHandler(event -> {
            if (this.node != null)
                moved(this.node);
        });
        new NodeShapeSelectionHandler(this) {
            @Override
            protected void onSelection() {
                if (NodeShape.this.node != null) {
                    selected(NodeShape.this.node);
                }
            }
        };

        updateNode(node);
    }

    public Node getNode() {
        return node;
    }

    public SlotShape getSlotShape(String slotId) {
        return slotShapes.get(slotId);
    }

    public void updateNode(Node node) {
        this.node = node;

        setX(this.node.getEditorSettings().getPositionX());
        setY(this.node.getEditorSettings().getPositionY());

        updateShape();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateSelectedState();
    }

    public boolean isSelected() {
        return selected;
    }

    protected void updateShape() {
        removeAll();

        double width = calculateWidth();
        double headerHeight = FlowDesignerConstants.PATCH_LABEL_FONT_SIZE + FlowDesignerConstants.PATCH_TITLE_FONT_SIZE + FlowDesignerConstants.PATCH_PADDING * 2;

        outline = new MultiPath();
        outline.M(0, headerHeight)
            .V(FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .Q(0, 0, FlowDesignerConstants.PATCH_CORNER_RADIUS, 0)
            .H(width - FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .Q(width, 0, width, FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .V(headerHeight);

        updateSlots(outline, width, headerHeight);

        if (node.findAllConnectableSlots().length == 0) {
            outline
                .Q(width, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS, width - FlowDesignerConstants.PATCH_CORNER_RADIUS, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS)
                .H(0 + FlowDesignerConstants.PATCH_CORNER_RADIUS)
                .Q(0, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS, 0, headerHeight- FlowDesignerConstants.PATCH_CORNER_RADIUS);
        }

        outline.Z();

        outline.setShadow(new Shadow(ColorName.DARKGRAY, 8, 1, 2));
        outline.setFillColor(ColorName.WHITE);
        add(outline);

        header = new MultiPath();
        header.M(0, FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .Q(1, 1, FlowDesignerConstants.PATCH_CORNER_RADIUS, 0)
            .H(width - FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .Q(width, 0, width, FlowDesignerConstants.PATCH_CORNER_RADIUS)
            .V(headerHeight);

        if (node.findAllConnectableSlots().length == 0) {
            header
                .Q(width, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS, width - FlowDesignerConstants.PATCH_CORNER_RADIUS, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS)
                .H(0 + FlowDesignerConstants.PATCH_CORNER_RADIUS)
                .Q(0, headerHeight + FlowDesignerConstants.PATCH_CORNER_RADIUS, 0, headerHeight- FlowDesignerConstants.PATCH_CORNER_RADIUS);

        } else {
            header.H(0);
        }

        header.Z();

        header.setFillColor(getPatchColor());
        header.setListening(false);
        add(header);

        if (!node.isLabelEmpty()) {
            patchLabel = new Text(node.getLabel(), FlowDesignerConstants.FONT_FAMILY, FlowDesignerConstants.PATCH_LABEL_FONT_SIZE);
            patchLabel.setFillColor(FlowDesignerConstants.PATCH_HEADER_TEXT_COLOR);
            patchLabel.setX(header.getBoundingBox().getWidth() / 2 - patchLabel.getBoundingBox().getWidth() / 2);
            patchLabel.setY(headerHeight / 2 + patchLabel.getBoundingBox().getHeight() / 2 + FlowDesignerConstants.PATCH_TITLE_FONT_SIZE / 2);
            patchLabel.setListening(false);
            add(patchLabel);
        }

        patchTypeLabel = new Text(node.getEditorSettings().getTypeLabel(), FlowDesignerConstants.FONT_FAMILY, FlowDesignerConstants.PATCH_TITLE_FONT_SIZE);
        patchTypeLabel.setFontStyle("italic");
        patchTypeLabel.setFillColor(FlowDesignerConstants.PATCH_HEADER_TEXT_COLOR);
        patchTypeLabel.setX(header.getBoundingBox().getWidth() / 2 - patchTypeLabel.getBoundingBox().getWidth() / 2);
        if (!node.isLabelEmpty()) {
            patchTypeLabel.setY(FlowDesignerConstants.PATCH_PADDING);
        } else {
            patchTypeLabel.setFontSize(FlowDesignerConstants.PATCH_LABEL_FONT_SIZE);
            patchTypeLabel.setY(headerHeight / 2 + patchTypeLabel.getBoundingBox().getHeight() / 2);
        }
        patchTypeLabel.setListening(false);
        add(patchTypeLabel);

        moveToBottom(outline);

        updateSelectedState();
    }

    protected double calculateWidth() {
        // Width depends on the node label and the combined width of widest sink and source slot labels
        int width = (int) FlowDesignerConstants.PATCH_MIN_WIDTH;
        Text patchLabel = new Text(node.getDefaultedLabel(), FlowDesignerConstants.FONT_FAMILY, FlowDesignerConstants.PATCH_LABEL_FONT_SIZE);
        width = (int) Math.max(width, patchLabel.getBoundingBox().getWidth());
        Text patchTypeLabel = new Text(node.getEditorSettings().getTypeLabel(), FlowDesignerConstants.FONT_FAMILY, FlowDesignerConstants.PATCH_TITLE_FONT_SIZE);
        if (!node.isLabelEmpty()) {
            patchTypeLabel.setFontSize(FlowDesignerConstants.PATCH_LABEL_FONT_SIZE);
        }
        width = (int) Math.max(width, patchTypeLabel.getBoundingBox().getWidth());

        int largestSource = 0;
        for (Slot source : node.findConnectableSlots(Slot.TYPE_SOURCE)) {
            Text slotLabel = new Text(
                !source.isLabelEmpty() ? source.getLabel() : FlowDesignerConstants.SLOT_SOURCE_LABEL,
                FlowDesignerConstants.FONT_FAMILY,
                FlowDesignerConstants.SLOT_FONT_SIZE
            );
            if (slotLabel.getBoundingBox().getWidth() > largestSource)
                largestSource = (int) slotLabel.getBoundingBox().getWidth();
        }


        int largestSink = 0;
        for (Slot sink : node.findConnectableSlots(Slot.TYPE_SINK)) {
            Text slotLabel = new Text(
                !sink.isLabelEmpty() ? sink.getLabel() : FlowDesignerConstants.SLOT_SINK_LABEL,
                FlowDesignerConstants.FONT_FAMILY,
                FlowDesignerConstants.SLOT_FONT_SIZE
            );
            if (slotLabel.getBoundingBox().getWidth() > largestSink)
                largestSink = (int) slotLabel.getBoundingBox().getWidth();
        }

        int maxSlotSpace = largestSink + largestSource + ((int) FlowDesignerConstants.PATCH_PADDING * 2);

        width = Math.max(width, maxSlotSpace);
        width += FlowDesignerConstants.PATCH_PADDING * 2;
        return width > 0 ? width : FlowDesignerConstants.PATCH_MIN_WIDTH;
    }

    protected IColor getPatchColor() {
        switch (node.getEditorSettings().getNodeColor()) {
            case SENSOR_ACTUATOR:
                return FlowDesignerConstants.PATCH_SENSOR_ACTUATOR_COLOR;
            case VIRTUAL:
                return FlowDesignerConstants.PATCH_VIRTUAL_COLOR;
            case CLIENT:
                return FlowDesignerConstants.PATCH_CLIENT_COLOR;
            default:
                return FlowDesignerConstants.PATCH_COLOR;
        }
    }

    protected void updateSlots(MultiPath outline, double x, double y) {
        Slot[] sources = node.findConnectableSlots(Slot.TYPE_SOURCE);
        Slot[] sinks = node.findConnectableSlots(Slot.TYPE_SINK);

        double slotHeight = (FlowDesignerConstants.SLOT_RADIUS * 2) + (FlowDesignerConstants.SLOT_PADDING * 4);

        for (Slot source : sources) {
            SlotShape slotShape = slotShapes.get(source.getId());
            if (slotShape == null) {
                slotShape = new SlotShape(this, source);
                slotShapes.put(source.getId(), slotShape);
            }
            y = updateSlotShape(slotShape, outline, x, y);
        }

        if (sinks.length > sources.length) {
            double diff = ((sinks.length - sources.length) * slotHeight);
            outline.V(y + diff);
            y += diff;
        }

        if (sources.length > 0 || sinks.length > 0) {
            outline.H(0);
        }

        if (sources.length > sinks.length) {
            double diff = ((sources.length - sinks.length) * slotHeight);
            outline.V(y - diff);
            y -= diff;
        }

        for (int i = sinks.length - 1; i >= 0; i--) {
            Slot sink = sinks[i];
            SlotShape slotShape = slotShapes.get(sink.getId());
            if (slotShape == null) {
                slotShape = new SlotShape(this, sink);
                slotShapes.put(sink.getId(), slotShape);
            }
            y = updateSlotShape(slotShape, outline, 0, y);
        }

        // Slots we had before but not anymore on the new node, have to remove wires
        Iterator<Map.Entry<String, SlotShape>> it = slotShapes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SlotShape> entry = it.next();
            if (node.findSlot(entry.getKey()) == null) {
                slotRemoved(entry.getValue());
                it.remove();
            }
        }
    }

    protected double updateSlotShape(SlotShape slotShape, MultiPath outline, double x, double y) {
        boolean isSource = slotShape.isSource();
        double slotDimensions = (FlowDesignerConstants.SLOT_RADIUS + FlowDesignerConstants.SLOT_PADDING) * 2;

        outline.V(isSource ? y + FlowDesignerConstants.SLOT_PADDING : y - FlowDesignerConstants.SLOT_PADDING);
        y = isSource ? y + FlowDesignerConstants.SLOT_PADDING : y - FlowDesignerConstants.SLOT_PADDING;

        outline.C(
            x + FlowDesignerConstants.SLOT_RADIUS + (FlowDesignerConstants.SLOT_PADDING * 2), y,
            x + FlowDesignerConstants.SLOT_RADIUS + (FlowDesignerConstants.SLOT_PADDING * 2), isSource ? y + slotDimensions : y - slotDimensions,
            x, isSource ? y + slotDimensions : y - slotDimensions
        );

        slotShape.updateNodeShape(this, x, y);

        y = isSource ? y + slotDimensions : y - slotDimensions;

        outline.V(isSource ? y + FlowDesignerConstants.SLOT_PADDING : y - FlowDesignerConstants.SLOT_PADDING);
        y = isSource ? y + FlowDesignerConstants.SLOT_PADDING : y - FlowDesignerConstants.SLOT_PADDING;

        return y;
    }

    protected void updateSelectedState() {
        if (outline != null)
            outline.setShadow(isSelected() ? new Shadow(ColorName.DIMGRAY, 5, 1, 2) : new Shadow(ColorName.DARKGRAY, 8, 1, 2));
        if (header != null)
            header.setFillColor(isSelected() ? FlowDesignerConstants.PATCH_SELECTED_COLOR : getPatchColor());
        if (patchLabel != null)
            patchLabel.setFillColor(isSelected() ? FlowDesignerConstants.PATCH_SELECTED_TEXT_COLOR : FlowDesignerConstants.PATCH_HEADER_TEXT_COLOR);
        if (patchTypeLabel != null)
            patchTypeLabel.setFillColor(isSelected() ? FlowDesignerConstants.PATCH_SELECTED_TEXT_COLOR : FlowDesignerConstants.PATCH_HEADER_TEXT_COLOR);
    }

    public abstract WireShape createWireShape(double x1, double y1, double x2, double y2, Slot source, Slot sink);

    public abstract WireShape[] getAttachedWireShapes(String slotId);

    protected abstract void selected(Node node);

    protected abstract void moved(Node node);

    protected abstract void slotRemoved(SlotShape slotShape);
}
