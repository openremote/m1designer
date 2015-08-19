package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Shadow;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.DragMode;
import com.ait.lienzo.shared.core.types.IColor;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.*;
import static org.openremote.beta.shared.flow.Node.*;
import static org.openremote.beta.shared.model.PropertyDescriptor.TYPE_DOUBLE;

public abstract class NodeShape extends Group {

    private static final Logger LOG = LoggerFactory.getLogger(NodeShape.class);

    protected Node node;
    protected Map<String, SlotShape> slotShapes = new HashMap<>();

    public NodeShape(Node node) {

        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);
        addNodeDragMoveHandler(event -> {
            if (this.node != null) {
                this.node.getEditorProperties().put(EDITOR_PROPERTY_X, getX());
                this.node.getEditorProperties().put(EDITOR_PROPERTY_Y, getY());
            }
        });
        addNodeDragEndHandler(event -> {
            if (this.node != null)
                moved(this.node);
        });
        new SelectionEventHandler(this) {
            @Override
            protected void onSelection() {
                if (NodeShape.this.node != null)
                    selected(NodeShape.this.node);
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

        setX(Properties.get(this.node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_X));
        setY(Properties.get(this.node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_Y));

        updateShape();
    }

    protected void updateShape() {
        removeAll();

        double width = calculateWidth();
        double headerHeight = PATCH_LABEL_FONT_SIZE + PATCH_TITLE_FONT_SIZE + PATCH_PADDING * 2;

        MultiPath outline = new MultiPath();
        outline.M(0, headerHeight)
            .V(PATCH_CORNER_RADIUS)
            .Q(0, 0, PATCH_CORNER_RADIUS, 0)
            .H(width - PATCH_CORNER_RADIUS)
            .Q(width, 0, width, PATCH_CORNER_RADIUS)
            .V(headerHeight);
        updateSlots(outline, width, headerHeight);
        outline.Z();
        outline.setShadow(new Shadow(ColorName.DARKGRAY, 8, 1, 2));
        outline.setFillColor(ColorName.WHITE);
        add(outline);

        MultiPath header = new MultiPath();
        header.M(0, PATCH_CORNER_RADIUS)
            .Q(1, 1, PATCH_CORNER_RADIUS, 0)
            .H(width - PATCH_CORNER_RADIUS)
            .Q(width, 0, width, PATCH_CORNER_RADIUS)
            .V(headerHeight)
            .H(0)
            .Z();
        header.setFillColor(getPatchColor());
        header.setListening(false);
        add(header);

        boolean haveNodeLabel = node.getLabel() != null && node.getLabel().length() > 0;

        if (haveNodeLabel) {
            Text patchLabel = new Text(node.getLabel(), FONT_FAMILY, PATCH_LABEL_FONT_SIZE);
            patchLabel.setFillColor(PATCH_LABEL_TEXT_COLOR);
            patchLabel.setX(header.getBoundingBox().getWidth() / 2 - patchLabel.getBoundingBox().getWidth() / 2);
            patchLabel.setY(headerHeight / 2 + patchLabel.getBoundingBox().getHeight() / 2 + PATCH_TITLE_FONT_SIZE / 2);
            patchLabel.setListening(false);
            add(patchLabel);
        }

        Text patchTypeLabel = new Text(Properties.get(this.node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL), FONT_FAMILY, PATCH_TITLE_FONT_SIZE);
        patchTypeLabel.setFontStyle("italic");
        patchTypeLabel.setFillColor(PATCH_TITLE_TEXT_COLOR);
        patchTypeLabel.setX(header.getBoundingBox().getWidth() / 2 - patchTypeLabel.getBoundingBox().getWidth() / 2);
        if (haveNodeLabel) {
            patchTypeLabel.setY(PATCH_PADDING);
        } else {
            patchTypeLabel.setFontSize(PATCH_LABEL_FONT_SIZE);
            patchTypeLabel.setY(headerHeight / 2 + patchTypeLabel.getBoundingBox().getHeight() / 2);
        }
        patchTypeLabel.setListening(false);
        add(patchTypeLabel);

        moveToBottom(outline);
    }

    protected double calculateWidth() {
        // Width depends on the node label and the combined width of widest sink and source slot labels
        double width = PATCH_MIN_WIDTH;
        Text patchLabel = new Text(node.getLabel(), FONT_FAMILY, PATCH_LABEL_FONT_SIZE);
        width = Math.max(width, patchLabel.getBoundingBox().getWidth());
        Text patchTypeLabel = new Text(Properties.get(this.node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL), FONT_FAMILY, PATCH_TITLE_FONT_SIZE);
        width = Math.max(width, patchTypeLabel.getBoundingBox().getWidth());

        double largestSource = 0;
        for (Slot source : node.findConnectableSlots(Slot.TYPE_SOURCE)) {
            Text slotLabel = new Text(source.getLabel(), FONT_FAMILY, SLOT_FONT_SIZE);
            if (slotLabel.getBoundingBox().getWidth() > largestSource)
                largestSource = slotLabel.getBoundingBox().getWidth();
        }

        double largestSink = 0;
        for (Slot sink : node.findConnectableSlots(Slot.TYPE_SINK)) {
            Text slotLabel = new Text(sink.getLabel(), FONT_FAMILY, SLOT_FONT_SIZE);
            if (slotLabel.getBoundingBox().getWidth() > largestSource)
                largestSink = slotLabel.getBoundingBox().getWidth();
        }

        double maxSlotSpace = largestSource + largestSink + PATCH_PADDING * 2;
        width = Math.max(width, maxSlotSpace);
        width += PATCH_PADDING * 2;
        return width > 0 ? width: PATCH_MIN_WIDTH;
    }

    protected IColor getPatchColor() {
        String color = Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_COLOR);
        switch (color != null ? NodeColor.valueOf(color) : NodeColor.DEFAULT) {
            case SENSOR_ACTUATOR:
                return PATCH_SENSOR_ACTUATOR_COLOR;
            case VIRTUAL:
                return PATCH_VIRTUAL_COLOR;
            case CLIENT:
                return PATCH_CLIENT_COLOR;
            default:
                return PATCH_COLOR;
        }
    }

    protected void updateSlots(MultiPath outline, double x, double y) {
        Slot[] sources = node.findConnectableSlots(Slot.TYPE_SOURCE);
        Slot[] sinks = node.findConnectableSlots(Slot.TYPE_SINK);

        double slotHeight = (SLOT_RADIUS + SLOT_PADDING) * 2;

        for (Slot source : sources) {
            SlotShape slotShape = slotShapes.get(source.getId());
            if (slotShape == null) {
                slotShape = new SlotShape(this, source);
                slotShapes.put(source.getId(), slotShape);
            }
            y = updateSlotShape(slotShape, outline, x, y);
        }

        if (sinks.length > sources.length) {
            double diff = ((sinks.length - sources.length) * slotHeight + (SLOT_PADDING * 2));
            outline.V(y + diff);
            y += diff;
        }

        outline.H(0);

        if (sources.length > sinks.length) {
            double diff = ((sources.length - sinks.length) * slotHeight + (SLOT_PADDING * 2));
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
        double slotDimensions = (SLOT_RADIUS + SLOT_PADDING) * 2;

        outline.V(isSource ? y + SLOT_PADDING : y - SLOT_PADDING);
        y = isSource ? y + SLOT_PADDING : y - SLOT_PADDING;

        outline.C(
            x + SLOT_RADIUS + (SLOT_PADDING * 2), y,
            x + SLOT_RADIUS + (SLOT_PADDING * 2), isSource ? y + slotDimensions : y - slotDimensions,
            x, isSource ? y + slotDimensions : y - slotDimensions
        );

        slotShape.updateNodeShape(this, x, y);

        y = isSource ? y + slotDimensions : y - slotDimensions;

        outline.V(isSource ? y + SLOT_PADDING : y - SLOT_PADDING);
        y = isSource ? y + SLOT_PADDING : y - SLOT_PADDING;

        return y;
    }

    public abstract WireShape createWireShape(double x1, double y1, double x2, double y2, Slot source, Slot sink);

    protected abstract void selected(Node node);

    protected abstract void moved(Node node);

    protected abstract void slotRemoved(SlotShape slotShape);
}
