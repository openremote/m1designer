package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.shape.BezierCurve;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.guides.ToolTip;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.DashArray;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ait.lienzo.client.core.Attribute.X;
import static com.ait.lienzo.client.core.Attribute.Y;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.*;

public abstract class WireShape extends Group {

    private static final Logger LOG = LoggerFactory.getLogger(WireShape.class);

    public abstract class Handle extends Circle {

        protected SlotShape attachedSlotShape;

        public Handle() {
            super(SLOT_RADIUS - SLOT_PADDING);

            setFillColor(WIRE_HANDLE_COLOR);
            setDraggable(true);
            setDragMode(DragMode.SAME_LAYER);

            addNodeDragMoveHandler(event -> {
                layout();
                SlotShape slotShape = findSlotShape(event.getX(), event.getY());
                if (slotShape != null) {
                    if (isAttachable(slotShape)) {
                        setFillColor(WIRE_HANDLE_ATTACH_COLOR);
                        setPosition(slotShape);
                    } else {
                        setFillColor(WIRE_HANDLE_ATTACH_VETO_COLOR);
                    }
                } else {
                    setFillColor(WIRE_HANDLE_COLOR);
                }
            });

            addNodeDragEndHandler(event -> {
                SlotShape slotShape = findSlotShape(event.getX(), event.getY());
                if (slotShape != null && isAttachable(slotShape)) {
                    setAttachedSlotShape(slotShape);
                }
            });
        }

        public void setPosition(SlotShape slotShape) {
            setX(slotShape.getX());
            setY(slotShape.getY());
            layout();
        }

        public void setAttachedSlotShape(SlotShape slotShape) {
            this.attachedSlotShape = slotShape;
            attachedSlotShape.getNodeShape().addAttributesChangedHandler(
                X, event -> {
                    setPosition(attachedSlotShape);
                }
            );
            attachedSlotShape.getNodeShape().addAttributesChangedHandler(
                Y, event -> {
                    setPosition(attachedSlotShape);
                }
            );
            setDraggable(false);
            setVisible(false);
            afterAttach();
        }

        public SlotShape getAttachedSlotShape() {
            return attachedSlotShape;
        }

        public Node getAttachedNode() {
            return getAttachedSlotShape() != null ? getAttachedSlotShape().getNodeShape().getNode() : null;
        }

        public Slot getAttachedSlot() {
            return getAttachedSlotShape() != null ? getAttachedSlotShape().getSlot() : null;
        }

        public void confirmRemove() {
            setVisible(true);
            setFillColor(WIRE_DELETE_COLOR);
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().getHandle().setVisible(false);
            }
        }

        public void cancelRemove() {
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().getHandle().setVisible(true);
            }
        }

        public void finalizeRemove() {
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().getHandle().setVisible(true);
            }
        }

        protected abstract void layout();

        protected abstract boolean isAttachable(SlotShape slotShape);

        protected abstract void afterAttach();
    }

    protected static Point2DArray calculateCurve(double x1, double y1, double x2, double y2) {
        // The plus/minus one is to avoid bleeding of the anti-aliased background
        return new Point2DArray(
            new Point2D(x1 - 1, y1),
            new Point2D(x1 + WIRE_CUBE_DISTANCE, y1),
            new Point2D(x2 - WIRE_CUBE_DISTANCE, y2),
            new Point2D(x2 + 1, y2)
        );
    }

    final protected BezierCurve curve;
    final protected Handle sourceHandle;
    final protected Handle sinkHandle;

    public WireShape(SlotShape sourceShape, SlotShape sinkShape) {
        this(
            sourceShape.getX(),
            sourceShape.getY(),
            sinkShape.getX(),
            sinkShape.getY()
        );
        getSourceHandle().setAttachedSlotShape(sourceShape);
        getSinkHandle().setAttachedSlotShape(sinkShape);
    }

    public WireShape(double x1, double y1, double x2, double y2) {

        sourceHandle = new Handle() {
            @Override
            protected void layout() {
                curve.setPoint2DArray(
                    calculateCurve(
                        getX() + SLOT_RADIUS - SLOT_PADDING, getY(),
                        sinkHandle.getX() - SLOT_RADIUS + SLOT_PADDING, sinkHandle.getY())
                );
                layoutChanged();
            }

            @Override
            protected boolean isAttachable(SlotShape slotShape) {
                return slotShape.getSlot().isOfType(Slot.TYPE_SOURCE)
                    && WireShape.this.isAttachable(
                    slotShape.getNodeShape().getNode(),
                    slotShape.getSlot(),
                    getSinkHandle().getAttachedNode(),
                    getSinkHandle().getAttachedSlot()
                );
            }


            @Override
            protected void afterAttach() {
                layout();
                WireShape.this.attached(
                    getSourceHandle().getAttachedNode(), getSourceHandle().getAttachedSlot(),
                    getSinkHandle().getAttachedNode(), getSinkHandle().getAttachedSlot()
                );
            }
        };
        sourceHandle.setX(x1);
        sourceHandle.setY(y1);

        sinkHandle = new Handle() {
            @Override
            protected void layout() {
                curve.setPoint2DArray(
                    calculateCurve(
                        sourceHandle.getX() + SLOT_RADIUS - SLOT_PADDING, sourceHandle.getY(),
                        getX() - SLOT_RADIUS + SLOT_PADDING, getY())
                );
                layoutChanged();
            }

            @Override
            protected boolean isAttachable(SlotShape slotShape) {
                return slotShape.getSlot().isOfType(Slot.TYPE_SINK)
                    && WireShape.this.isAttachable(
                    getSourceHandle().getAttachedNode(),
                    getSourceHandle().getAttachedSlot(),
                    slotShape.getNodeShape().getNode(),
                    slotShape.getSlot()
                );
            }

            @Override
            protected void afterAttach() {
                layout();
                WireShape.this.attached(
                    getSourceHandle().getAttachedNode(), getSourceHandle().getAttachedSlot(),
                    getSinkHandle().getAttachedNode(), getSinkHandle().getAttachedSlot()
                );
            }
        };
        sinkHandle.setX(x2);
        sinkHandle.setY(y2);

        this.curve = new BezierCurve(0, 0, 0, 0, 0, 0, 0, 0);
        curve.setStrokeColor(WIRE_COLOR);
        curve.setStrokeWidth(WIRE_WIDTH);
        layout();

        add(sourceHandle);
        add(sinkHandle);
        add(curve);
    }

    public Handle getSourceHandle() {
        return sourceHandle;
    }

    public Handle getSinkHandle() {
        return sinkHandle;
    }

    public void startRemove() {
        curve.getDashArray();
        curve.setDashArray(WIRE_WIDTH);
        moveToTop();
    }

    public void cancelRemove() {
        curve.setDashArray(new DashArray());
        getSourceHandle().cancelRemove();
        getSinkHandle().cancelRemove();
    }

    public void confirmRemove() {
        curve.setStrokeColor(WIRE_DELETE_COLOR);
        getSourceHandle().confirmRemove();
        getSinkHandle().confirmRemove();
    }

    public void finalizeRemove() {
        getSourceHandle().finalizeRemove();
        getSinkHandle().finalizeRemove();
        removeAll();
        removeFromParent();
        detached(
            getSourceHandle().getAttachedNode(), getSourceHandle().getAttachedSlot(),
            getSinkHandle().getAttachedNode(), getSinkHandle().getAttachedSlot()
        );
    }

    public boolean isAttached(String slotId) {
        if (sinkHandle.getAttachedSlot() != null && sinkHandle.getAttachedSlot().getId().equals(slotId))
            return true;
        if (sourceHandle.getAttachedSlot() != null && sourceHandle.getAttachedSlot().getId().equals(slotId))
            return true;
        return false;
    }

    public void activateToolTip(ToolTip toolTip, String title, String text) {
        curve.addNodeMouseEnterHandler(event -> {
            toolTip.setValues(text, title);
            final BoundingBox bb = curve.getBoundingBox();
            toolTip.show(
                curve.getX() + bb.getX() + (bb.getWidth() / 2),
                curve.getY() + bb.getY() + (bb.getHeight() / 2) - (WIRE_WIDTH * 2)
            );
        });
        curve.addNodeMouseExitHandler(event -> toolTip.hide());
        curve.addNodeDragStartHandler(event -> toolTip.hide());
    }

    protected void layout() {
        // Calculate curve
        sourceHandle.layout();
        sinkHandle.layout();
    }

    abstract protected void layoutChanged();

    abstract protected SlotShape findSlotShape(int x, int y);

    abstract protected boolean isAttachable(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);


    abstract protected void attached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);

    abstract protected void detached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);
}