package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.FlowEvent;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

@JsType
public class NodeAddedEvent extends FlowEvent {

    final public Node node;
    final public double positionX, positionY;
    final public boolean transformPosition;

    public NodeAddedEvent(Flow flow, Node node, double positionX, double positionY, boolean transformPosition) {
        super(flow);
        this.node = node;
        this.positionX = positionX;
        this.positionY = positionY;
        this.transformPosition = transformPosition;
    }

    public Node getNode() {
        return node;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public boolean isTransformPosition() {
        return transformPosition;
    }
}
