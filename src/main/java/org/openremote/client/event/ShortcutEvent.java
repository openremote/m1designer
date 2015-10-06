package org.openremote.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.Event;

@JsType
public class ShortcutEvent extends Event {

    public final int key;

    public ShortcutEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
