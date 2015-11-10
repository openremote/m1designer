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

import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.Node;

public abstract class NodeShapeSelectionHandler
    implements NodeMouseClickHandler, NodeTouchStartHandler, NodeTouchCancelHandler, NodeTouchMoveHandler, NodeTouchEndHandler {

    protected boolean selected;

    public NodeShapeSelectionHandler() {
    }

    public NodeShapeSelectionHandler(Node node) {
        node.addNodeMouseClickHandler(this);
        node.addNodeTouchStartHandler(this);
        node.addNodeTouchCancelHandler(this);
        node.addNodeTouchMoveHandler(this);
        node.addNodeTouchEndHandler(this);
    }

    @Override
    public void onNodeMouseClick(NodeMouseClickEvent event) {
        onSelection();
    }

    @Override
    public void onNodeTouchStart(NodeTouchStartEvent event) {
        selected = true;
    }

    @Override
    public void onNodeTouchCancel(NodeTouchCancelEvent event) {
        selected = false;
    }

    @Override
    public void onNodeTouchMove(NodeTouchMoveEvent event) {
        selected = false;
    }

    @Override
    public void onNodeTouchEnd(NodeTouchEndEvent event) {
        if (selected)
            onSelection();
        selected = false;
    }

    protected abstract void onSelection();

}
