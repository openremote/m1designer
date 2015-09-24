package org.openremote.beta.client.shell.floweditor;

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

public abstract class WireShape extends Group {

    private static final Logger LOG = LoggerFactory.getLogger(WireShape.class);

    public static int HELP_SHOWN = 0;

    public abstract class Handle extends Circle {

        protected SlotShape attachedSlotShape;

        public Handle() {
            super(FlowDesignerConstants.SLOT_RADIUS - FlowDesignerConstants.SLOT_PADDING);

            setFillColor(FlowDesignerConstants.WIRE_HANDLE_COLOR);
            setDraggable(true);
            setDragMode(DragMode.SAME_LAYER);

            addNodeDragMoveHandler(event -> {
                layout();
                SlotShape slotShape = findSlotShape(event.getX(), event.getY());
                if (slotShape != null) {
                    if (isAttachable(slotShape)) {
                        setFillColor(FlowDesignerConstants.WIRE_HANDLE_ATTACH_COLOR);
                        setPosition(slotShape);
                    } else {
                        setFillColor(FlowDesignerConstants.WIRE_HANDLE_ATTACH_VETO_COLOR);
                    }
                } else {
                    setFillColor(FlowDesignerConstants.WIRE_HANDLE_COLOR);
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
            setFillColor(FlowDesignerConstants.WIRE_DELETE_COLOR);
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

        @Override
        public String toString() {
            return attachedSlotShape != null ? attachedSlotShape.getSlot().toString() : "NOT ATTACHED";
        }

        protected abstract void layout();

        protected abstract boolean isAttachable(SlotShape slotShape);

        protected abstract void afterAttach();
    }

    protected static Point2DArray calculateCurve(double x1, double y1, double x2, double y2) {
        // The plus/minus one avoids edge bleed
        return new Point2DArray(
            new Point2D(x1 - FlowDesignerConstants.SLOT_PADDING + 1, y1),
            new Point2D(x1 + FlowDesignerConstants.WIRE_CUBE_DISTANCE, y1),
            new Point2D(x2 - FlowDesignerConstants.WIRE_CUBE_DISTANCE, y2),
            new Point2D(x2 + FlowDesignerConstants.SLOT_PADDING - 1, y2)
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
                        getX() + FlowDesignerConstants.SLOT_RADIUS - FlowDesignerConstants.SLOT_PADDING, getY(),
                        sinkHandle.getX() - FlowDesignerConstants.SLOT_RADIUS + FlowDesignerConstants.SLOT_PADDING, sinkHandle.getY())
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
                        sourceHandle.getX() + FlowDesignerConstants.SLOT_RADIUS - FlowDesignerConstants.SLOT_PADDING, sourceHandle.getY(),
                        getX() - FlowDesignerConstants.SLOT_RADIUS + FlowDesignerConstants.SLOT_PADDING, getY())
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
        curve.setStrokeColor(FlowDesignerConstants.WIRE_COLOR);
        curve.setStrokeWidth(FlowDesignerConstants.WIRE_WIDTH);
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
        curve.setDashArray(FlowDesignerConstants.WIRE_WIDTH);
        moveToTop();
    }

    public void cancelRemove() {
        curve.setDashArray(new DashArray());
        getSourceHandle().cancelRemove();
        getSinkHandle().cancelRemove();
    }

    public void confirmRemove() {
        curve.setStrokeColor(FlowDesignerConstants.WIRE_DELETE_COLOR);
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
            if (HELP_SHOWN <= FlowDesignerConstants.TOOLTIP_MAX_SHOW_TIMES) {
                toolTip.setValues(text, title);
                final BoundingBox bb = curve.getBoundingBox();
                toolTip.show(
                    curve.getX() + bb.getX() + (bb.getWidth() / 2),
                    curve.getY() + bb.getY() + (bb.getHeight() / 2) - (FlowDesignerConstants.WIRE_WIDTH * 2)
                );
                HELP_SHOWN = HELP_SHOWN + 1;
            }
        });
        curve.addNodeMouseExitHandler(event -> {

            toolTip.hide();
        });
        curve.addNodeDragStartHandler(event -> toolTip.hide());
    }

    public void setPulse(boolean pulse) {
        if (pulse) {
            curve.setStrokeWidth(FlowDesignerConstants.WIRE_WIDTH * 1.5);
        } else {
            curve.setStrokeWidth(FlowDesignerConstants.WIRE_WIDTH);
        }
    }

    protected void layout() {
        // Calculate curve
        sourceHandle.layout();
        sinkHandle.layout();
    }

    @Override
    public String toString() {
        return "WireShape{" +
            "source=" + sourceHandle +
            ", sink=" + sinkHandle +
            "}";
    }

    abstract protected void layoutChanged();

    abstract protected SlotShape findSlotShape(int x, int y);

    abstract protected boolean isAttachable(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);


    abstract protected void attached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);

    abstract protected void detached(Node sourceNode, Slot sourceSlot, Node sinkNode, Slot sinkSlot);
}