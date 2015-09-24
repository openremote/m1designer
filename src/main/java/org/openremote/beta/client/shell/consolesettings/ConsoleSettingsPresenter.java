package org.openremote.beta.client.shell.consolesettings;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.event.ConsoleEditModeEvent;
import org.openremote.beta.client.event.ShellCloseEvent;
import org.openremote.beta.client.event.ConsoleZoomEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ConsoleSettingsPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleSettingsPresenter.class);

    public boolean editMode = false;

    public ConsoleSettingsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void toggleEditMode() {
        switchEditMode(!editMode);
    }

    public void switchEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyPath("editMode", editMode);
        dispatch(new ConsoleEditModeEvent(editMode));
    }

    public void zoom(double factor) {
        dispatch(new ConsoleZoomEvent(factor));
    }
    
    public void exit() {
        dispatch(new ShellCloseEvent());
    }
}
