package org.openremote.beta.client.flow;

import com.ait.lienzo.client.core.shape.BezierCurve;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.guides.ToolTip;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Slot;

import java.util.logging.Logger;

import static com.ait.lienzo.client.core.Attribute.X;
import static com.ait.lienzo.client.core.Attribute.Y;
import static org.openremote.beta.client.flow.Constants.*;

public abstract class WireShape extends Group {

    private static final Logger LOG = Logger.getLogger(WireShape.class.getName());

    public abstract class Handle extends Box {

        protected SlotShape attachedSlotShape;

        public Handle() {
            super(
                SLOT_CORNER_RADIUS,
                WIRE_HANDLE_COLOR,
                new TextLabel(TextLabel.space(SLOT_PADDING), FONT_FAMILY, SLOT_FONT_SIZE, WIRE_HANDLE_ATTACH_TEXT_COLOR),
                SLOT_PADDING
            );

            setDraggable(true);
            setDragMode(DragMode.SAME_LAYER);

            addNodeDragMoveHandler(event -> {
                layout();
                SlotShape slotShape = findSlotShape(event.getX(), event.getY());
                if (slotShape != null) {
                    if (isAttachable(slotShape)) {
                        setFillColor(WIRE_HANDLE_ATTACH_COLOR);
                        setText(slotShape.getText());
                        setWidth(slotShape.getWidth(), true);
                        setHeight(slotShape.getHeight(), true);
                        setPosition(slotShape);
                    } else {
                        setFillColor(WIRE_HANDLE_ATTACH_VETO_COLOR);
                        setText(TextLabel.space(SLOT_PADDING));
                    }
                } else {
                    setFillColor(WIRE_HANDLE_COLOR);
                    setText(TextLabel.space(SLOT_PADDING));
                    setWidth(0);
                    setHeight(0);
                }
            });

            addNodeDragEndHandler(event -> {
                SlotShape slotShape = findSlotShape(event.getX(), event.getY());
                if (slotShape != null) {
                    setAttachedSlotShape(slotShape);
                }
            });

        }

        public void setPosition(SlotShape slotShape) {
            setX(slotShape.getParent().getAttributes().getX() + slotShape.getX());
            setY(slotShape.getParent().getAttributes().getY() + slotShape.getY());
            layout();
        }

        public void setAttachedSlotShape(SlotShape slotShape) {
            this.attachedSlotShape = slotShape;
            attachedSlotShape.getParent().addAttributesChangedHandler(
                X, event -> setPosition(attachedSlotShape)
            );
            attachedSlotShape.getParent().addAttributesChangedHandler(
                Y, event -> setPosition(attachedSlotShape)
            );
            setText(TextLabel.space(SLOT_PADDING));
            setWidth(slotShape.getWidth(), true);
            setHeight(slotShape.getHeight(), true);
            setDraggable(false);
            setVisible(false);
            afterAttach();
        }

        public SlotShape getAttachedSlotShape() {
            return attachedSlotShape;
        }

        public void confirmRemove() {
            setVisible(true);
            setFillColor(WIRE_DELETE_COLOR);
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().setVisible(false);
            }
        }

        public void cancelRemove() {
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().setVisible(true);
            }
        }

        public void finalizeRemove() {
            if (getAttachedSlotShape() != null) {
                getAttachedSlotShape().setVisible(true);
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
            sourceShape.getParent().getX() + sourceShape.getX(),
            sourceShape.getParent().getY() + sourceShape.getY(),
            sinkShape.getParent().getX() + sinkShape.getX(),
            sinkShape.getParent().getY() + sinkShape.getY()
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
                        getX() + getWidth(), getY() + getHeight() / 2,
                        sinkHandle.getX(), sinkHandle.getY() + sinkHandle.getHeight() / 2
                    )
                );
            }

            @Override
            protected boolean isAttachable(SlotShape slotShape) {
                return slotShape.getSlot().getType() == Slot.Type.SOURCE
                    && WireShape.this.isAttachable(
                    slotShape.getSlot(),
                    sinkHandle.getAttachedSlotShape() != null ? sinkHandle.getAttachedSlotShape().getSlot() : null
                );
            }


            @Override
            protected void afterAttach() {
                layout();
                WireShape.this.attached(
                    sourceHandle.getAttachedSlotShape() != null ? sourceHandle.getAttachedSlotShape().getSlot() : null,
                    sinkHandle.getAttachedSlotShape() != null ? sinkHandle.getAttachedSlotShape().getSlot() : null
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
                        sourceHandle.getX() + sourceHandle.getWidth(), sourceHandle.getY() + sourceHandle.getHeight() / 2,
                        getX(), getY() + getHeight() / 2
                    )
                );
            }

            @Override
            protected boolean isAttachable(SlotShape slotShape) {
                return slotShape.getSlot().getType() == Slot.Type.SINK
                    && WireShape.this.isAttachable(
                    sourceHandle.getAttachedSlotShape() != null ? sourceHandle.getAttachedSlotShape().getSlot() : null,
                    slotShape.getSlot()
                );
            }

            @Override
            protected void afterAttach() {
                layout();
                WireShape.this.attached(
                    sourceHandle.getAttachedSlotShape() != null ? sourceHandle.getAttachedSlotShape().getSlot() : null,
                    sinkHandle.getAttachedSlotShape() != null ? sinkHandle.getAttachedSlotShape().getSlot() : null
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
        curve.setDashArray(WIRE_WIDTH);
        moveToTop();
    }

    public void cancelRemove() {
        curve.setDashArray(0);
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
            getSourceHandle().getAttachedSlotShape() != null ? getSourceHandle().getAttachedSlotShape().getSlot() : null,
            getSinkHandle().getAttachedSlotShape() != null ? getSinkHandle().getAttachedSlotShape().getSlot() : null
        );
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

    abstract protected SlotShape findSlotShape(int x, int y);

    abstract protected boolean isAttachable(Slot sourceSlot, Slot sinkSlot);

    abstract protected void attached(Slot sourceSlot, Slot sinkSlot);

    abstract protected void detached(Slot sourceSlot, Slot sinkSlot);
}