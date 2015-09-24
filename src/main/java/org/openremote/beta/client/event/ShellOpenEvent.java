package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Flow;

@JsType
public class ShellOpenEvent extends FlowEvent {

    public ShellOpenEvent(Flow flow) {
        super(flow);
    }
}
