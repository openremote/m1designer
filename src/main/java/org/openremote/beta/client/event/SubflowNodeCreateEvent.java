package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class SubflowNodeCreateEvent extends FlowEvent {

    final public String subflowId;
    final public double positionX;
    final public double positionY;
    final public boolean applyPositionAsProperties;

    public SubflowNodeCreateEvent(Flow flow, String subflowId, double positionX, double positionY) {
        this(flow, subflowId, positionX, positionY, false);
    }

    public SubflowNodeCreateEvent(Flow flow, String subflowId, double positionX, double positionY, boolean applyPositionAsProperties) {
        super(flow);
        this.subflowId = subflowId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.applyPositionAsProperties = applyPositionAsProperties;
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

    public boolean isApplyPositionAsProperties() {
        return applyPositionAsProperties;
    }
}
