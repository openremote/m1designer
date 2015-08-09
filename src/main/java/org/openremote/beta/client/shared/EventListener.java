package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsFunction;
import org.openremote.beta.shared.event.Event;

@JsFunction
public interface EventListener<E extends Event> {
    void on(E event);
}
