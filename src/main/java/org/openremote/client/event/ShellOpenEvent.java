package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.flow.Flow;

@JsType
public class ShellOpenEvent extends FlowEvent {

    public ShellOpenEvent(Flow flow) {
        super(flow);
    }
}
