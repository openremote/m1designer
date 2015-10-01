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
