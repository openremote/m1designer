package org.openremote.beta.server.web.socket;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

public class WebsocketConsumer extends DefaultConsumer {

    private final WebsocketEndpoint endpoint;

    public WebsocketConsumer(WebsocketEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        endpoint.connect(this);
    }

    @Override
    public void doStop() throws Exception {
        endpoint.disconnect(this);
        super.doStop();
    }

    public WebsocketEndpoint getEndpoint() {
        return endpoint;
    }

    public void sendMessage(final String sessionKey, final String message) {
        sendMessage(sessionKey, (Object)message);
    }

    public void sendMessage(final String sessionKey, final Object message) {
        final Exchange exchange = getEndpoint().createExchange();
        exchange.getIn().setHeader(WebsocketConstants.SESSION_KEY, sessionKey);
        exchange.getIn().setBody(message);

        getAsyncProcessor().process(exchange, doneSync -> {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        });
    }

}
