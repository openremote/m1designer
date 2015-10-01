package org.openremote.client.shared;

import com.google.gwt.core.client.js.JsFunction;
import org.openremote.shared.event.Event;

@JsFunction
public interface EventListener<E extends Event> {
    void on(E event);
}
