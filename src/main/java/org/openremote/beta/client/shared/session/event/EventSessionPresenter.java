package org.openremote.beta.client.shared.session.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.SessionClosedCleanEvent;
import org.openremote.beta.client.shared.session.SessionConnectEvent;
import org.openremote.beta.client.shared.session.SessionPresenter;
import org.openremote.beta.shared.event.Event;
import org.openremote.beta.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EventSessionPresenter extends SessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EventSessionPresenter.class);

    final protected EventCodec EVENT_CODEC = GWT.create(EventCodec.class);

    public EventSessionPresenter(com.google.gwt.dom.client.Element view) {
        super(view, getWebSocketUrl("events"));

        addEventListener(SessionClosedCleanEvent.class, event -> {
            // We want to stay connected indefinitely, even if the server
            // drops the connection clean after maxIdleTime
            dispatchEvent(new SessionConnectEvent());
        });

        addEventListener(EventSessionConnectEvent.class, event -> {
            dispatchEvent(new SessionConnectEvent());
        });

        addEventListener(MessageSendEvent.class, event -> {
            dispatchEvent(false, new ServerSendEvent(event.getMessage()));
        });

        addEventListener(ServerSendEvent.class, event -> {
            sendData(EVENT_CODEC.encode(event.getEvent()).toString());
        });
    }

    @Override
    protected void onDataReceived(String data) {
        Event event = EVENT_CODEC.decode(data);
        if (event.getType().equals(Event.getType(Message.class))) {
            Message message = (Message) event;
            dispatchEvent(new MessageReceivedEvent(message));
        } else {
            dispatchEvent(new ServerReceivedEvent(event));
        }
    }
}
