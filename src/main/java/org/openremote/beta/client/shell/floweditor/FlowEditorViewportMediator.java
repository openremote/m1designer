package org.openremote.beta.client.shell.floweditor;

import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.mediator.AbstractMediator;
import com.ait.lienzo.client.core.mediator.IEventFilter;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.event.shared.GwtEvent;

import java.util.List;

public class FlowEditorViewportMediator extends AbstractMediator {

    private Point2D lastPosition = new Point2D();
    private boolean dragging = false;
    private List<TouchPoint> touches;
    private double lastTouchDistance;

    public FlowEditorViewportMediator() {
        LienzoPanel.enableWindowMouseWheelScroll(true);
    }

    @Override
    public boolean handleEvent(GwtEvent<?> event) {
        if (event.getAssociatedType() == NodeMouseWheelEvent.getType()) {
            IEventFilter filter = getEventFilter();
            if ((null == filter) || !filter.isEnabled() || (filter.test(event))) {
                onMouseWheel((NodeMouseWheelEvent) event);
                return true;
            }
        } else if (event.getAssociatedType() == NodeMouseDownEvent.getType()
            || event.getAssociatedType() == NodeTouchStartEvent.getType()) {

            int x = ((INodeXYEvent) event).getX();
            int y = ((INodeXYEvent) event).getY();

            // Ignore if there was sourceHandle shape under the pointer, instead drag the shape
            Shape shape = getViewport().getScene().findShapeAtPoint(x, y);
            if (shape != null) {
                return false;
            }

            if (dragging && event.getAssociatedType() == NodeTouchStartEvent.getType()) {
                onMultiTouchStart(((NodeTouchStartEvent) event).getTouches());
            } else {
                onDragScreenStart(x, y);
            }
            return true;
        } else if (event.getAssociatedType() == NodeMouseMoveEvent.getType()
            || event.getAssociatedType() == NodeTouchMoveEvent.getType()) {

            int x = ((INodeXYEvent) event).getX();
            int y = ((INodeXYEvent) event).getY();

            if (touches != null && event.getAssociatedType() == NodeTouchMoveEvent.getType()) {
                onMultiTouchMove(((NodeTouchMoveEvent) event).getTouches());
                return true;
            } else if (dragging) {
                onDragScreen(x, y);
                return true;
            }
        } else if (event.getAssociatedType() == NodeMouseUpEvent.getType()
            || event.getAssociatedType() == NodeTouchEndEvent.getType()
            || event.getAssociatedType() == NodeTouchCancelEvent.getType()) {

            if (touches != null && event.getAssociatedType() != NodeMouseUpEvent.getType()) {
                onMultiTouchEnd();
                return true;
            } else if (dragging) {
                onDragScreenEnd();
                return true;
            }
        }

        return false;
    }

    @Override
    public void cancel() {
        dragging = false;
        touches = null;
    }

    protected void onMouseWheel(NodeMouseWheelEvent event) {
        double scaleDelta;

        if (event.isSouth() != FlowDesignerConstants.ZOOM_INVERT) {
            scaleDelta = 1 / (1 + FlowDesignerConstants.ZOOM_FACTOR);
        } else {
            scaleDelta = 1 + FlowDesignerConstants.ZOOM_FACTOR;
        }

        onZoomScreen(
            scaleDelta,
            FlowDesignerConstants.ZOOM_MIN_SCALE,
            FlowDesignerConstants.ZOOM_MAX_SCALE,
            event.getX(),
            event.getY()
        );
    }

    protected void onMultiTouchStart(List<TouchPoint> touches) {
        this.touches = touches;
    }

    protected void onMultiTouchMove(List<TouchPoint> touches) {
        this.touches = touches;

        if (touches.size() < 2)
            return;

        // We only care about the first two fingers and their midpoint/distance
        Point2D a = new Point2D(touches.get(0).getX(), touches.get(0).getY());
        Point2D b = new Point2D(touches.get(1).getX(), touches.get(1).getY());
        Point2D mid = a.add(b).div(2);
        double distance = a.distance(b);

        if (FlowDesignerConstants.ZOOM_TOUCH_THRESHOLD > 0) {
            double delta = distance - lastTouchDistance;
            if ((delta > 0 && delta < FlowDesignerConstants.ZOOM_TOUCH_THRESHOLD)
                || (delta <0 && delta > -FlowDesignerConstants.ZOOM_TOUCH_THRESHOLD))
                return;
        }

        boolean zoomOut = distance < lastTouchDistance;
        double scaleDelta;

        if (zoomOut != FlowDesignerConstants.ZOOM_INVERT) {
            scaleDelta = 1 / (1 + FlowDesignerConstants.ZOOM_FACTOR * 2);
        } else {
            scaleDelta = 1 + FlowDesignerConstants.ZOOM_FACTOR * 2;
        }

        onZoomScreen(
            scaleDelta,
            FlowDesignerConstants.ZOOM_MIN_SCALE,
            FlowDesignerConstants.ZOOM_MAX_SCALE,
            new Double(mid.getX()).intValue(),
            new Double(mid.getY()).intValue()
        );

        lastTouchDistance = distance;
    }

    protected void onMultiTouchEnd() {
        cancel();
    }

    protected void onZoomScreen(double scaleDelta, double min, double max, int x, int y) {
        Transform transform = getTransform();

        if (transform == null) {
            setTransform(transform = new Transform());
        }

        double currentScale = transform.getScaleX();
        double newScale = currentScale * scaleDelta;

        if (newScale < min) {
            scaleDelta = min / currentScale;
        }
        if (newScale > max) {
            scaleDelta = max / currentScale;
        }
        Point2D p = new Point2D(x, y);

        transform.getInverse().transform(p, p);

        transform = transform.copy();

        transform.scaleAboutPoint(scaleDelta, p.getX(), p.getY());

        setTransform(transform);

        if (isBatchDraw()) {
            getViewport().getScene().batch();
        } else {
            getViewport().getScene().draw();
        }
    }

    protected void onDragScreenStart(int x, int y) {
        lastPosition = new Point2D(x, y);

        dragging = true;

        Transform transform = getTransform();

        if (transform == null) {
            setTransform(new Transform());
        }
    }

    protected void onDragScreen(int x, int y) {
        Point2D currentPosition = new Point2D(x, y);

        Transform transform = getTransform().copy();

        double distanceX = currentPosition.getX() - lastPosition.getX();
        double distanceY = currentPosition.getY() - lastPosition.getY();

        transform.translate(distanceX / transform.getScaleX(), distanceY / transform.getScaleY());

        setTransform(transform);

        lastPosition = currentPosition;

        if (isBatchDraw()) {
            getViewport().getScene().batch();
        } else {
            getViewport().getScene().draw();
        }
    }

    protected void onDragScreenEnd() {
        cancel();
    }

}
