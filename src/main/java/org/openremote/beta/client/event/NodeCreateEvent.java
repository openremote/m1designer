package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class NodeCreateEvent extends FlowEvent {

    final public String nodeType;
    final public double positionX;
    final public double positionY;

    public NodeCreateEvent(Flow flow, String nodeType, double positionX, double positionY) {
        super(flow);
        this.nodeType = nodeType;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public String getNodeType() {
        return nodeType;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }
}
