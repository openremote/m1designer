package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.Function;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConfirmationEvent extends Event {

    final public String title;
    final public String text;
    final public Function confirmAction;
    final public Function cancelAction;

    public ConfirmationEvent(String title, String text, Function confirmAction) {
        this(title, text, confirmAction, null);
    }

    public ConfirmationEvent(String title, String text, Function confirmAction, Function cancelAction) {
        this.title = title;
        this.text = text;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public Function getConfirmAction() {
        return confirmAction;
    }

    public Function getCancelAction() {
        return cancelAction;
    }
}
