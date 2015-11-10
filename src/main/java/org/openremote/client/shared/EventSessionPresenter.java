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

package org.openremote.client.shared;

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsType;
import org.openremote.client.event.*;
import org.openremote.shared.event.Event;
import org.openremote.shared.event.Message;
import org.openremote.shared.event.client.MessageReceivedEvent;
import org.openremote.shared.event.client.MessageSendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class EventSessionPresenter<V extends View> extends SessionPresenter<V> {

    private static final Logger LOG = LoggerFactory.getLogger(EventSessionPresenter.class);

    final protected EventCodec EVENT_CODEC = GWT.create(EventCodec.class);

    public EventSessionPresenter(V view) {
        super(view, getWebSocketUrl("events"));

        addListener(SessionClosedCleanEvent.class, event -> {
            // We want to stay connected indefinitely, even if the server
            // drops the connection clean after maxIdleTime
            dispatch(new SessionConnectEvent());
        });

        addListener(EventSessionConnectEvent.class, event -> {
            dispatch(new SessionConnectEvent());
        });

        addListener(MessageSendEvent.class, event -> {
            dispatch(new ServerSendEvent(event.getMessage()));
        });

        addListener(ServerSendEvent.class, event -> {
            sendData(EVENT_CODEC.encode(event.getEvent()).toString());
        });
    }

    @Override
    protected void onDataReceived(String data) {
        Event event = EVENT_CODEC.decode(data);
        if (event.getType().equals(Event.getType(Message.class))) {
            Message message = (Message) event;
            dispatch(new MessageReceivedEvent(message));
        } else {
            dispatch(event);
        }
    }
}
