package org.openremote.beta.client.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConsoleEditModeEvent extends Event {

    public final boolean editMode;

    public ConsoleEditModeEvent(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }
}
