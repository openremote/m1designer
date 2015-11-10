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

import com.ait.lienzo.client.core.shape.*;
import com.ait.lienzo.client.core.shape.guides.ToolTip;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public abstract class FlowDesigner {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDesigner.class);

    class NodeShapeImpl extends NodeShape {

        public NodeShapeImpl(Node node) {
            super(node);
        }

        @Override
        public WireShape createWireShape(double x1, double y1, double x2, double y2, Slot source, Slot sink) {
            WireShape wireShape = new WireShapeImpl(x1, y1, x2, y2);
            wiringLayer.add(wireShape);
            FlowDesigner.this.getLayer().batch();
            return wireShape;
        }


        @Override
        protected void selected(Node node) {
            selectNodeShape(node);
            onSelection(node);
        }

        @Override
        protected void moved(Node node) {
            onMoved(node);
        }

        @Override
        protected void slotRemoved(SlotShape slotShape) {
            for (WireShape attachedWireShape : getAttachedWireShapes(slotShape.getSlot().getId())) {
                attachedWireShape.finalizeRemove();
            }
        }

        @Override
        public WireShape[] getAttachedWireShapes(String slotId) {
            return FlowDesigner.this.getAttachedWireShapes(slotId);
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
        protected void layoutChanged() {
            FlowDesigner.this.getLayer().batch();
        }

        @Override
        protected SlotShape findSlotShape(int x, int y) {
            Shape<?> shape = FlowDesigner.this.getLayer().findShapeAtPoint(x, y);
            if (shape != null) {
                if (shape instanceof SlotShape.Handle) {
                    SlotShape.Handle slotHandle = (SlotShape.Handle) shape;
                    return slotHandle.getSlotShape();
                }
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
            if (sourceSlot != null && sinkSlot != null) {
                Wire wire = flow.addWireBetweenSlots(sourceSlot, sinkSlot);
                if (wire != null) {
                    onAddition(wire);
                }
            }
            updateSlots(sourceSlot, sinkSlot);
        }

        @Override
        protected void detached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot) {
            if (sourceSlot != null && sinkSlot != null) {
                Wire wire = flow.removeWireBetweenSlots(sourceSlot, sinkSlot);
                if (wire != null)
                    onRemoval(wire);
            }
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
                    continue;
                slotShape.setAttached(flow.hasWires(slot.getId()));
            }
            FlowDesigner.this.getLayer().batch();
        }
    }

    final protected Flow flow;
    protected Text emptyFlowMessage;

    final protected WiringLayer wiringLayer = new WiringLayer() {
        @Override
        protected void batchAll() {
            FlowDesigner.this.getLayer().batch();
        }
    };

    final protected Layer layer = new Layer() {
        @Override
        public Layer batch() {
            wiringLayer.batch();
            return super.batch();
        }
    };

    final protected ToolTip toolTip;

    public FlowDesigner(Flow flow, Scene scene) {
        this.flow = flow;

        scene.add(layer);
        scene.add(wiringLayer);

        this.toolTip = new ToolTip()
            .setAutoHideTime(FlowDesignerConstants.TOOLTIP_AUTO_HIDE_MILLIS)
            .setFillColor(FlowDesignerConstants.TOOLTIP_BACKGROUND_COLOR);
        getLayer().getViewport().getOverLayer().add(toolTip);

        if (flow.getNodes().length == 0) {
            showEmptyFlowMessage();
            layer.batch();
        }

        for (Node node : flow.getNodes()) {
            addNodeShape(node);
        }
        for (Wire wire : flow.getWires()) {
            addWireShape(wire);
        }
    }

    public Flow getFlow() {
        return flow;
    }

    public void viewPortChanged() {
        toolTip.hide();
    }

    public void handleMessage(Message event) {
        SlotShape slotShape = getSlotShape(event.getSlotId());
        if (slotShape != null) {
            slotShape.setSlotValue(event.getBody());
            getLayer().batch();
        }
    }

    public void updateNodeShape(Node node) {
        NodeShape nodeShape = getNodeShape(node.getId());
        if (nodeShape != null) {
            nodeShape.updateNode(node);
            getLayer().batch();
        }
    }

    public void addNodeShape(Node node) {
        getLayer().add(new NodeShapeImpl(node));
        hideEmptyFlowMessage();
        getLayer().batch();
    }

    public void deleteNodeShape(Node node) {
        NodeShape nodeShape = getNodeShape(node.getId());
        getLayer().remove(nodeShape);

        for (Slot slot : node.getSlots()) {
            WireShape[] wireShapes = getAttachedWireShapes(slot.getId());
            for (WireShape wireShape : wireShapes) {
                wireShape.finalizeRemove();
            }
        }

        if (flow.getNodes().length == 0) {
            showEmptyFlowMessage();
        }

        getLayer().batch();

        selectNodeShape(null);
    }

    public void selectNodeShape(Node node) {
        for (Node n : flow.getNodes()) {
            NodeShape nodeShape = getNodeShape(n.getId());
            if (nodeShape == null)
                continue;
            nodeShape.setSelected(node != null && n.getId().equals(node.getId()));
        }
        getLayer().batch();
    }

    protected Layer getLayer() {
        return layer;
    }

    protected void addWireShape(Wire wire) {
        SlotShape sourceShape = getSlotShape(wire.getSourceId());
        SlotShape sinkShape = getSlotShape(wire.getSinkId());
        if (sourceShape != null && sinkShape != null) {
            wiringLayer.add(new WireShapeImpl(sourceShape, sinkShape));
        }
        getLayer().batch();
    }

    protected WireShape[] getAttachedWireShapes(String slotId) {
        Set<WireShape> collection = new HashSet<>();
        for (IPrimitive<?> primitive : wiringLayer.getChildNodes()) {
            if (primitive instanceof WireShape) {
                WireShape wireShape = (WireShape) primitive;
                if (wireShape.isAttached(slotId))
                    collection.add(wireShape);
            }
        }
        return collection.toArray(new WireShape[collection.size()]);
    }

    protected SlotShape getSlotShape(String slotId) {
        for (IPrimitive<?> primitive : getLayer().getChildNodes()) {
            if (primitive instanceof NodeShape) {
                NodeShape nodeShape = (NodeShape) primitive;
                SlotShape slotShape = nodeShape.getSlotShape(slotId);
                if (slotShape != null)
                    return slotShape;
            }
        }
        return null;
    }

    protected NodeShape getNodeShape(String nodeId) {
        for (IPrimitive<?> primitive : getLayer().getChildNodes()) {
            if (primitive instanceof NodeShape) {
                NodeShape nodeShape = (NodeShape) primitive;
                if (nodeShape.node.getId().equals(nodeId))
                    return nodeShape;
            }
        }
        return null;
    }

    protected void showEmptyFlowMessage() {
        emptyFlowMessage = new Text(
            "Drag & drop device sensors, actuators, elements, and other flows here.",
            FlowDesignerConstants.FONT_FAMILY,
            12
        );
        emptyFlowMessage.setFillColor(FlowDesignerConstants.PATCH_COLOR);
        emptyFlowMessage.setX(layer.getWidth() / 2 - emptyFlowMessage.getBoundingBox().getWidth() / 2);
        emptyFlowMessage.setY(layer.getHeight() / 2 - emptyFlowMessage.getBoundingBox().getHeight() / 2);
        layer.add(emptyFlowMessage);
    }

    protected void hideEmptyFlowMessage() {
        if (emptyFlowMessage != null) {
            layer.remove(emptyFlowMessage);
            emptyFlowMessage = null;
        }
    }

    protected abstract void onSelection(Node node);

    protected abstract void onMoved(Node node);

    protected abstract void onAddition(Wire wire);

    protected abstract void onRemoval(Wire wire);

}
