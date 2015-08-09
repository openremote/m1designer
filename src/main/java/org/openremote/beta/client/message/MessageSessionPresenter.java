package org.openremote.beta.client.message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import org.openremote.beta.client.shared.SessionClosedCleanEvent;
import org.openremote.beta.client.shared.SessionConnectEvent;
import org.openremote.beta.client.shared.SessionPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageSessionPresenter extends SessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSessionPresenter.class);

    final protected MessageEventCodec MESSAGE_CODEC = GWT.create(MessageEventCodec.class);

    final protected String serviceUrl;

    public MessageSessionPresenter(Element view) {
        super(view);

        this.serviceUrl = "ws://" + hostname() + ":9292/message";

        addEventListener(MessageServerConnectEvent.class, event -> {
            connectAndRetryOnFailure(serviceUrl, 12, 5000); // TODO one minute?
        });

        addEventListener(SessionClosedCleanEvent.class, event -> {
            // Try to reconnect
            dispatchEvent(new SessionConnectEvent(serviceUrl));
        });

        addEventListener(MessageSendEvent.class, action -> {
            sendMessage(MESSAGE_CODEC.encode(action.getMessageEvent()).toString());
        });
    }

    @Override
    protected void onMessageReceived(String data) {
        dispatchEvent(MESSAGE_CODEC.decode(data));
    }
}
