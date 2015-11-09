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
