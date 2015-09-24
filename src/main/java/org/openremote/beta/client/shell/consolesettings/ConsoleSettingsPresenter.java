package org.openremote.beta.client.shell.consolesettings;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.console.ConsoleEditModeEvent;
import org.openremote.beta.client.console.ConsoleMaximizeEvent;
import org.openremote.beta.client.console.ConsoleZoomEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ConsoleSettingsPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleSettingsPresenter.class);

    public ConsoleSettingsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void switchEditMode(boolean editMode) {
        dispatchEvent(new ConsoleEditModeEvent(editMode));
    }

    public void zoom(double factor) {
        dispatchEvent(new ConsoleZoomEvent(factor));
    }
    
    public void exit() {
        dispatchEvent(new ConsoleMaximizeEvent());
    }
}
