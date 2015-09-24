package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

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
