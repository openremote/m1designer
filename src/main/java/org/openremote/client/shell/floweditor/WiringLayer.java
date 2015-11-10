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

import com.ait.lienzo.client.core.shape.BezierCurve;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.Point2D;

public abstract class WiringLayer extends Layer {

    class WireRemovalConfirmationHandler {

        public WireShape wireShape;
        public Point2D start;
        public boolean removalConfirmed;

        public void onInteraction(int x, int y) {
            if (wireShape == null || removalConfirmed)
                return;
            Point2D point = new Point2D(x, y);
            if (point.distance(start) > FlowDesignerConstants.WIRE_DELETE_DISTANCE) {
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
            if (shape != null && shape instanceof BezierCurve) {
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
