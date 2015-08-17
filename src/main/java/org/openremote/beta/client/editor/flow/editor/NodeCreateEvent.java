package org.openremote.beta.client.editor.flow.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
@JsExport
public class NodeCreateEvent extends Event{

    final protected String nodeType;
    final protected double positionX;
    final protected double positionY;

    public NodeCreateEvent(String nodeType, double positionX, double positionY) {
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
