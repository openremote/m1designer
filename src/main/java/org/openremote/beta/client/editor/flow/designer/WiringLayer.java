package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.Point2D;

import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.WIRE_DELETE_DISTANCE;

public abstract class WiringLayer extends Layer {

    class WireRemovalConfirmationHandler {

        public WireShape wireShape;
        public Point2D start;
        public boolean removalConfirmed;

        public void onInteraction(int x, int y) {
            if (wireShape == null || removalConfirmed)
                return;
            Point2D point = new Point2D(x, y);
            if (point.distance(start) > WIRE_DELETE_DISTANCE) {
                removalConfirmed = true;
                wireShape.confirmRemove();
                batchAll();
            }
        }

        public void reset() {
            this.wireShape = null;
            this.start = null;
            this.removalConfirmed = false;
        }
    }

    class WireRemovalStartHandler {
        public void onInteraction(int x, int y) {
            Shape shape = findShapeAtPoint(x, y);
            if (shape != null && shape.getParent() instanceof WireShape) {
                wireRemovalConfirmationHandler.wireShape = (WireShape) shape.getParent();
                wireRemovalConfirmationHandler.start = new Point2D(x, y);
                wireRemovalConfirmationHandler.removalConfirmed = false;
                wireRemovalConfirmationHandler.wireShape.startRemove();
                batchAll();
            }
        }
    }

    class WireRemovalStopHandler {
        public void onInteraction(int x, int y) {
            if (wireRemovalConfirmationHandler.wireShape != null) {
                if (wireRemovalConfirmationHandler.removalConfirmed) {
                    wireRemovalConfirmationHandler.wireShape.finalizeRemove();
                    wireRemovalConfirmationHandler.reset();
                    batchAll();
                } else {
                    wireRemovalConfirmationHandler.wireShape.cancelRemove();
                    wireRemovalConfirmationHandler.reset();
                    batchAll();
                }
            }
        }
    }

    final protected WireRemovalStartHandler wireRemovalStartHandler = new WireRemovalStartHandler();
    final protected WireRemovalConfirmationHandler wireRemovalConfirmationHandler = new WireRemovalConfirmationHandler();
    final protected WireRemovalStopHandler wireRemovalStopHandler = new WireRemovalStopHandler();

    public WiringLayer() {
        addNodeMouseMoveHandler(
            event -> wireRemovalConfirmationHandler.onInteraction(event.getX(), event.getY())
        );
        addNodeTouchMoveHandler(
            event -> wireRemovalConfirmationHandler.onInteraction(event.getX(), event.getY())
        );

        addNodeMouseDownHandler(
            event -> wireRemovalStartHandler.onInteraction(event.getX(), event.getY())
        );
        addNodeTouchStartHandler(
            event -> wireRemovalStartHandler.onInteraction(event.getX(), event.getY())
        );


        addNodeMouseUpHandler(
            event -> wireRemovalStopHandler.onInteraction(event.getX(), event.getY())
        );
        addNodeTouchCancelHandler(
            event -> wireRemovalStopHandler.onInteraction(event.getX(), event.getY())
        );
        addNodeTouchEndHandler(
            event -> wireRemovalStopHandler.onInteraction(event.getX(), event.getY())
        );
    }

    protected abstract void batchAll();
}