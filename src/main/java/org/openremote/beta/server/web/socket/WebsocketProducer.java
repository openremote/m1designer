package org.openremote.beta.server.web.socket;

import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Collection;

public class WebsocketProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketProducer.class);

    private final Boolean sendToAll;
    private final WebsocketEndpoint endpoint;

    public WebsocketProducer(WebsocketEndpoint endpoint) {
        super(endpoint);
        this.sendToAll = endpoint.getSendToAll();
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        Object message = in.getMandatoryBody();
        if (!(message == null || message instanceof String || message instanceof byte[])) {
            message = in.getMandatoryBody(String.class);
        }
        if (isSendToAllSet(in)) {
            sendToAll(message, exchange);
        } else {
            String sessionKey = in.getHeader(WebsocketConstants.SESSION_KEY, String.class);
            if (sessionKey != null) {
                Session websocket = getEndpoint().getComponent().getWebsocketSessions().get(sessionKey);
                sendMessage(websocket, message);
            } else {
                throw new IllegalArgumentException("Failed to send message to Websocket session; session key not set.");
            }
        }
    }

    public WebsocketEndpoint getEndpoint() {
        return endpoint;
    }

    protected boolean isSendToAllSet(Message in) {
        Boolean value = in.getHeader(WebsocketConstants.SEND_TO_ALL, sendToAll, Boolean.class);
        return value == null ? false : value;
    }

    protected void sendToAll(Object message, Exchange exchange) throws Exception {
        Collection<Session> sessions = getEndpoint().getComponent().getWebsocketSessions().getAll();
        LOG.trace("Sending to all sessions ({}): {}", sessions.size(), message);

        Exception exception = null;
        for (Session session : sessions) {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                if (exception == null) {
                    exception = new CamelExchangeException("Failed to deliver message to one or more recipients.", exchange, e);
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected void sendMessage(Session session, Object message) throws IOException {
        if (session != null && session.isOpen()) {
            LOG.trace("Sending to session {}: {}", session.getId(), message);
            if (message instanceof String) {
                session.getBasicRemote().sendText((String) message);
            }
        }
    }
}
