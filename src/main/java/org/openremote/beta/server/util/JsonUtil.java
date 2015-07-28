package org.openremote.beta.server.util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class JsonUtil {

    // TODO This only catches errors when consuming JSON, no idea how to intercept dataformat/producer errors
    public static class JsonProcessingExceptionHandler implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            Throwable cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
            exchange.getOut().setBody(cause.getMessage());
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
            exchange.getContext().createProducerTemplate()
                .send("log:org.openremote.beta.json?level=WARN&showCaughtException=true&showBodyType=false&showExchangePattern=false", exchange);
        }
    }

}
