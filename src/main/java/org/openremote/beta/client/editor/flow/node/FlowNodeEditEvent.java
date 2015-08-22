package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

@JsType
public class FlowNodeEditEvent extends FlowEvent {

    final protected Node node;

    public FlowNodeEditEvent(Flow flow, Node node) {
        super(flow);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
