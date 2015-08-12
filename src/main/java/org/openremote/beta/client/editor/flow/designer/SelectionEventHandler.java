package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.event.*;
import com.ait.lienzo.client.core.shape.Node;

public abstract class SelectionEventHandler
    implements NodeMouseClickHandler, NodeTouchStartHandler, NodeTouchCancelHandler, NodeTouchMoveHandler, NodeTouchEndHandler {

    protected boolean selected;

    public SelectionEventHandler() {
    }

    public SelectionEventHandler(Node node) {
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
