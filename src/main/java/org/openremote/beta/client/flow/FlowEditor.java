package org.openremote.beta.client.flow;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Scene;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.shape.guides.ToolTip;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;

import java.util.logging.Logger;

import static org.openremote.beta.client.flow.Constants.TOOLTIP_AUTO_HIDE_MILLIS;
import static org.openremote.beta.client.flow.Constants.TOOLTIP_BACKGROUND_COLOR;

public class FlowEditor extends Layer {

    private static final Logger LOG = Logger.getLogger(FlowEditor.class.getName());

    final protected Flow flow;
    final protected WiringLayer wiringLayer = new WiringLayer() {
        @Override
        protected void batchAll() {
            FlowEditor.this.batch();
        }
    };
    final protected ToolTip toolTip;

    class WireShapeImpl extends WireShape {

        public WireShapeImpl(SlotShape sourceShape, SlotShape sinkShape) {
            super(sourceShape, sinkShape);
            activateToolTip(toolTip, "Wire", "Drag to remove.");
        }

        public WireShapeImpl(double x1, double y1, double x2, double y2) {
            super(x1, y1, x2, y2);
            activateToolTip(toolTip, "Wire", "Drag to remove.");
        }

        @Override
        protected SlotShape findSlotShape(int x, int y) {
            Shape<?> shape = FlowEditor.this.findShapeAtPoint(x, y);
            if (shape != null && shape.getParent() instanceof SlotShape) {
                return (SlotShape) shape.getParent();
            }
            return null;
        }

        @Override
        protected boolean isAttachable(Slot sourceSlot, Slot sinkSlot) {
            // Can't attach a wire between two slots if they are already wired
            for (Wire wire : flow.getWires()) {
                if (wire.equals(sourceSlot, sinkSlot))
                    return false;
            }

            // Can't attach a wire between slots of the same node
            if (sourceSlot != null && sinkSlot != null
                && flow.findOwnerNode(sourceSlot.getId()).getId().equals(
                flow.findOwnerNode(sinkSlot.getId()).getId()))
                return false;
            return true;
        }

        @Override
        protected void attached(Slot sourceSlot, Slot sinkSlot) {
            if (sourceSlot != null && sinkSlot != null) {
                flow.addWire(sourceSlot, sinkSlot);
                updateSlots(sourceSlot, sinkSlot);
            }
        }

        @Override
        protected void detached(Slot sourceSlot, Slot sinkSlot) {
            if (sourceSlot != null && sinkSlot != null) {
                flow.removeWire(sourceSlot, sinkSlot);
                updateSlots(sourceSlot, sinkSlot);
            }
        }
    }

    public FlowEditor(Flow flow, Scene scene) {
        this.flow = flow;

        scene.add(this);
        scene.add(wiringLayer);

        this.toolTip = new ToolTip()
            .setAutoHideTime(TOOLTIP_AUTO_HIDE_MILLIS)
            .setFillColor(TOOLTIP_BACKGROUND_COLOR);
        getViewport().getOverLayer().add(toolTip);
        getViewport().addViewportTransformChangedHandler(event -> toolTip.hide());

        for (Node node : flow.getNodes()) {
            add(node);
        }
        for (Wire wire : flow.getWires()) {
            add(wire);
        }
    }

    public Flow getFlow() {
        return flow;
    }

    @Override
    public Layer batch() {
        wiringLayer.batch();
        return super.batch();
    }

    protected void add(Node node) {
        Slots slots = new Slots() {
            @Override
            protected WireShape createWireShape(double x1, double y1, double x2, double y2, Slot source, Slot sink) {
                WireShape wireShape = new WireShapeImpl(x1, y1, x2, y2);
                wiringLayer.add(wireShape);
                batch();
                return wireShape;
            }
        };
        add(new NodeShape(node, slots));
        add(slots);
        batch();
    }

    protected void add(Wire wire) {
        SlotShape sourceShape = getSlotShape(wire.getSourceId());
        SlotShape sinkShape = getSlotShape(wire.getSinkId());
        if (sourceShape != null && sinkShape != null) {
            wiringLayer.add(new WireShapeImpl(sourceShape, sinkShape));
        } else {
            LOG.warning("Unknown source/sink slots, skipping: " + wire);
        }
        batch();
    }

    protected SlotShape getSlotShape(String slotId) {
        for (IPrimitive<?> primitive : getChildNodes()) {
            if (primitive instanceof NodeShape) {
                NodeShape nodeShape = (NodeShape) primitive;
                SlotShape slotShape = nodeShape.getSlots().getSlotShape(slotId);
                if (slotShape != null)
                    return slotShape;
            }
        }
        return null;
    }

    protected void updateSlots(Slot... slots) {
        for (Slot slot : slots) {
            if (slot == null)
                continue;
            SlotShape slotShape = getSlotShape(slot.getId());
            if (slotShape == null)
                return;
            slotShape.setAttached(flow.hasWires(slot.getId()));
        }
        batch();
    }
}
