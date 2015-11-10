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

package org.openremote.client.shell;

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import jsinterop.annotations.JsType;
import org.openremote.client.event.*;
import org.openremote.client.shared.EventSessionPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.client.*;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class ShellPresenter extends EventSessionPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public boolean shellOpened = false;
    public boolean consoleHasWidgets = false;

    public ShellPresenter(View view) {
        super(view);

        addListener(ShellOpenEvent.class, event -> {
            shellOpened = true;
            notifyPath("shellOpened", shellOpened);
        });

        addListener(ShellCloseEvent.class, event -> {
            shellOpened = false;
            notifyPath("shellOpened", shellOpened);
        });

        addListener(ConsoleRefreshedEvent.class, event-> {
            consoleHasWidgets = event.isRenderedWidgets();
            notifyPath("consoleHasWidgets", consoleHasWidgets);
        });

        addListener(ShowInfoEvent.class, event -> {
            getView().fire(event.getType(), event);
        });

        addListener(ShowFailureEvent.class, event -> {
            getView().fire(event.getType(), event);
        });

        addListener(RequestCompleteEvent.class, event -> {
            getView().fire(event.getType(), event);
        });

        addListener(ConfirmationEvent.class, event-> {
            getViewChildComponent("#confirmationDialog").fire(event.getType(), event);
        });

        addListener(InventoryManagerOpenEvent.class, event -> {
            getView().fire(event.getType(), event);
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatch(new EventSessionConnectEvent());

        ClientPresetVariant clientPresetVariant = new ClientPresetVariant(
            Browser.getWindow().getNavigator().getUserAgent(),
            Browser.getWindow().getScreen().getWidth(),
            Browser.getWindow().getScreen().getHeight()
        );
        loadPresetFlow(clientPresetVariant);

        if (Browser.getWindow().getLocation().getHash().equals("#shell")) {
            dispatch(new ShellOpenEvent(null));
        }
    }

    public void onShortcutKey(int key) {
        LOG.debug("Shortcut key pressed: " + key);
        dispatch(new ShortcutEvent(key));
    }

    public void exit() {
        dispatch(new ShellCloseEvent());
    }

    protected void loadPresetFlow(ClientPresetVariant clientPresetVariant) {
        sendRequest(
            false,
            false,
            resource("flow", "preset")
                .addQueryParam("agent", clientPresetVariant.getUserAgent())
                .addQueryParam("width", Integer.toString(clientPresetVariant.getWidthPixels()))
                .addQueryParam("height", Integer.toString(clientPresetVariant.getHeightPixels()))
                .get(),
            new ObjectResponseCallback<Flow>("Load preset flow", FLOW_CODEC) {
                @Override
                protected void onResponse(Flow flow) {
                    dispatch(new ConsoleRefreshEvent(flow));
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    if (requestFailure != null && requestFailure.statusCode == 404) {
                        LOG.debug("No preset flow found...");
                    } else {
                        dispatch(
                            new ShowFailureEvent("Can't initialize panel. " + requestFailure.getFailureMessage()));
                    }
                }
            }
        );
    }

}
