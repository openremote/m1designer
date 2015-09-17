package org.openremote.beta.server.web;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UndertowErrorHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowErrorHandler.class);

    final protected boolean devMode;
    final protected HttpHandler nextHandler;

    public UndertowErrorHandler(boolean devMode, HttpHandler nextHandler) {
        this.devMode = devMode;
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            nextHandler.handleRequest(exchange);
        } catch (Exception ex) {

            LOG.debug(exchange.getRequestMethod() + " " + exchange.getRequestPath(), ex);

            if (exchange.isResponseChannelAvailable()) {
                try {
                    if (devMode) {
                        exchange.getResponseSender().send(renderTrace(ex));
                    } else {
                        exchange.getResponseSender().send(renderGeneric());
                    }
                } catch (Exception ex2) {
                    LOG.error("Couldn't render server error response (in DEVMODE)", ex2);
                }
            }
        }
    }

    protected String renderTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return "500 Server Error\n\n" + sw.toString();
    }

    protected static String renderGeneric() {
        return "Request failed due to server error, please check logs and contact the help desk.";
    }

}
