package org.openremote.client.shared.session.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.client.shared.session.SessionClosedCleanEvent;
import org.openremote.client.shared.session.SessionConnectEvent;
import org.openremote.client.shared.session.SessionPresenter;
import org.openremote.shared.event.Event;
import org.openremote.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EventSessionPresenter extends SessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EventSessionPresenter.class);

    final protected EventCodec EVENT_CODEC = GWT.create(EventCodec.class);

    public EventSessionPresenter(com.google.gwt.dom.client.Element view) {
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
