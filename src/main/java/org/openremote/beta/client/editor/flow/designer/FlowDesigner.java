package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Scene;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.shape.guides.ToolTip;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.TOOLTIP_AUTO_HIDE_MILLIS;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.TOOLTIP_BACKGROUND_COLOR;

public abstract class FlowDesigner extends Layer {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDesigner.class);

    class NodeShapeImpl extends NodeShape {

        public NodeShapeImpl(Node node, Slots slots) {
            super(node, slots);
        }

        @Override
        protected void selected(Node node) {
            onSelectionNode(node);
        }
    }

    class WireShapeImpl extends WireShape {

        public WireShapeImpl(SlotShape sourceShape, SlotShape sinkShape) {
            super(sourceShape, sinkShape);
            activateToolTip();
        }

        public WireShapeImpl(double x1, double y1, double x2, double y2) {
            super(x1, y1, x2, y2);
            activateToolTip();
        }

        @Override
        protected SlotShape findSlotShape(int x, int y) {
            Shape<?> shape = FlowDesigner.this.findShapeAtPoint(x, y);
            if (shape != null && shape.getParent() instanceof SlotShape) {
                return (SlotShape) shape.getParent();
            }
            return null;
        }

        @Override
        protected boolean isAttachable(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot) {
            // Can't attach a wire between two slots if they are already wired
            for (Wire wire : flow.getWires()) {
                if (wire.equalsSlots(sourceSlot, sinkSlot))
                    return false;
            }

            // Can't attach a wire between slots of the same node
            if (sourceNode.getId().equals(sinkNode.getId()))
                return false;

            return true;
        }

        @Override
        protected void attached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot) {
            if (sourceSlot != null && sinkSlot != null)
                flow.addWireBetweenSlots(sourceSlot, sinkSlot);
            updateSlots(sourceSlot, sinkSlot);
        }

        @Override
        protected void detached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot) {
            if (sourceSlot != null && sinkSlot != null)
                flow.removeWire(sourceSlot, sinkSlot);
            updateSlots(sourceSlot, sinkSlot);
        }

        protected void activateToolTip() {
            activateToolTip(toolTip, "Wire", "Drag to remove.");
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

    final protected Flow flow;

    final protected WiringLayer wiringLayer = new WiringLayer() {
        @Override
        protected void batchAll() {
            FlowDesigner.this.batch();
        }
    };

    final protected ToolTip toolTip;

    public FlowDesigner(Flow flow, Scene scene) {
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

    public void receiveMessageEvent(MessageEvent event) {
        SlotShape slotShape = getSlotShape(event.getSinkSlotId());
        if (slotShape != null) {
            slotShape.setLabelValue(event.getBody());
            batch();
        }
    }

    public void add(Node node) {
        Slots slots = new Slots(node) {
            @Override
            protected WireShape createWireShape(double x1, double y1, double x2, double y2, Slot source, Slot sink) {
                WireShape wireShape = new WireShapeImpl(x1, y1, x2, y2);
                wiringLayer.add(wireShape);
                batch();
                return wireShape;
            }
        };
        add(new NodeShapeImpl(node, slots));
        add(slots);
        batch();
    }

    public void add(Wire wire) {
        SlotShape sourceShape = getSlotShape(wire.getSourceId());
        SlotShape sinkShape = getSlotShape(wire.getSinkId());
        if (sourceShape != null && sinkShape != null) {
            wiringLayer.add(new WireShapeImpl(sourceShape, sinkShape));
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

    protected abstract void onSelectionNode(Node node);
}
