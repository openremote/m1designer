package org.openremote.beta.client.shell.event;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.Callback;
import org.openremote.beta.shared.event.Event;

@JsType
public class ConfirmationEvent extends Event {

    final public String title;
    final public String text;
    final public Callback confirmAction;
    final public Callback cancelAction;
    final public String otherActionText;
    final public Callback otherAction;

    public ConfirmationEvent(String title, String text, Callback confirmAction) {
        this(title, text, confirmAction, null);
    }

    public ConfirmationEvent(String title, String text, Callback confirmAction, String otherActionText, Callback otherAction) {
        this(title, text, confirmAction, null, otherActionText, otherAction);
    }

    public ConfirmationEvent(String title, String text, Callback confirmAction, Callback cancelAction) {
        this(title, text, confirmAction, cancelAction, null, null);
    }

    public ConfirmationEvent(String title, String text, Callback confirmAction, Callback cancelAction, String otherActionText, Callback otherAction) {
        this.title = title;
        this.text = text;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;
        this.otherActionText = otherActionText;
        this.otherAction = otherAction;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public Callback getConfirmAction() {
        return confirmAction;
    }

    public Callback getCancelAction() {
        return cancelAction;
    }

    public String getOtherActionText() {
        return otherActionText;
    }

    public Callback getOtherAction() {
        return otherAction;
    }
}
