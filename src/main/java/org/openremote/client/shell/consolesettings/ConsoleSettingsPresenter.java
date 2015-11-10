/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
