package org.openremote.beta.client.shell.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shell.event.FlowEvent;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class SubflowNodeCreateEvent extends FlowEvent {

    final public String subflowId;
    final public double positionX;
    final public double positionY;

    public SubflowNodeCreateEvent(Flow flow, String subflowId, double positionX, double positionY) {
        super(flow);
        this.subflowId = subflowId;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public String getSubflowId() {
        return subflowId;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }
}
