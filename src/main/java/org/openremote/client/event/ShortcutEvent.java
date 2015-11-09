package org.openremote.client.event;

import jsinterop.annotations.JsType;
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
