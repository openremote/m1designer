package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class ConsoleRefreshedEvent extends Event {

    final public boolean renderedWidgets;

    public ConsoleRefreshedEvent(boolean renderedWidgets) {
        this.renderedWidgets = renderedWidgets;
    }

    public boolean isRenderedWidgets() {
        return renderedWidgets;
    }
}
