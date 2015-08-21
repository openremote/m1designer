package org.openremote.beta.client.shared.session.message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.session.SessionConnectEvent;
import org.openremote.beta.client.shared.session.SessionPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageSessionPresenter extends SessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSessionPresenter.class);

    final protected MessageEventCodec MESSAGE_CODEC = GWT.create(MessageEventCodec.class);

    public MessageSessionPresenter(com.google.gwt.dom.client.Element view) {
        super(view, getWebSocketUrl("message"));

        addEventListener(MessageServerConnectEvent.class, event -> {
            dispatchEvent(new SessionConnectEvent());
        });

        addEventListener(MessageSendEvent.class, event -> {
            sendMessage(MESSAGE_CODEC.encode(event.getMessageEvent()).toString());
        });
    }

    @Override
    protected void onMessageReceived(String data) {
        dispatchEvent(new MessageReceivedEvent(MESSAGE_CODEC.decode(data)));
    }
}
