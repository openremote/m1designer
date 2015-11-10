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
import com.google.gwt.json.client.JSONValue;
import elemental.client.Browser;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.openremote.client.event.EventCodec;
import org.openremote.client.event.EventSessionConnectEvent;
import org.openremote.client.shared.EventSessionPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.Event;
import org.openremote.shared.event.bus.EventRegistration;
import org.openremote.shared.event.client.ShellReadyEvent;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class ShellEmbeddedPresenter extends EventSessionPresenter<ShellEmbeddedPresenter.ShellEmbeddedView> {

    private static final Logger LOG = LoggerFactory.getLogger(ShellEmbeddedPresenter.class);

    final protected EventCodec EVENT_CODEC = GWT.create(EventCodec.class);

    final protected EventRegistration catchAllRegistration;

    @JsType(isNative = true)
    public interface ShellEmbeddedView extends View {
        void publishShellEvent(String msg);
    }

    public ShellEmbeddedPresenter(ShellEmbeddedView view) {
        super(view);

        // Catch all events from presenters, publish serializable events on the native WebView bridge
        catchAllRegistration = addListener(event -> {
            JSONValue eventValue = EVENT_CODEC.encode(event);
            // If we can't encode it, it shouldn't be send on the native bridge by definition
            if (eventValue != null) {
                LOG.debug("Publishing on embedded shell message bus: " + event.getType());
                getView().publishShellEvent(eventValue.toString());
            } else {
                LOG.debug("Not publishing on embedded shell message bus: " + event.getType());
            }
        });
    }

    @Override
    public void attached() {
        super.attached();
        
        // TODO Websocket should be done by the native application, not the WebView!
        dispatch(new EventSessionConnectEvent());

        ClientPresetVariant clientPresetVariant = new ClientPresetVariant(
            Browser.getWindow().getNavigator().getUserAgent(),
            Browser.getWindow().getScreen().getWidth(),
            Browser.getWindow().getScreen().getHeight()
        );

        dispatch(new ShellReadyEvent(clientPresetVariant));
    }

    // Receive events from the native WebView bridge and dispatch them to presenters
    public void onShellEvent(String msg) {
        Event event;
        try {
            event = EVENT_CODEC.decode(msg);
        } catch (Exception ex) {
            LOG.debug("Received unsupported event message: " + msg);
            return;
        }
        LOG.debug("On embedded shell message bus, received event: " + event.getType());
        dispatch(event, catchAllRegistration);
    }

}
