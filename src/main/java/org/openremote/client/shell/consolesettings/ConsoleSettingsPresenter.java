package org.openremote.client.shell.consolesettings;

import jsinterop.annotations.JsType;
import org.openremote.client.event.ShortcutEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.client.ConsoleEditModeEvent;
import org.openremote.shared.event.client.ConsoleZoomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class ConsoleSettingsPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleSettingsPresenter.class);

    public boolean editMode = false;

    public ConsoleSettingsPresenter(View view) {
        super(view);

        addListener(ShortcutEvent.class, event -> {
            if (event.getKey() == 69)
                toggleEditMode();
        });
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
}
