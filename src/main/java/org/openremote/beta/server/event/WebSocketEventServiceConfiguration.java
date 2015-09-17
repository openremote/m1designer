package org.openremote.beta.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEventServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventServiceConfiguration.class);

    public static final String WEBSOCKET_EVENTS = "events";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                // Receive events on the websocket and publish them on the local bus
                from("uws://" + WEBSOCKET_EVENTS)
                    .routeId("Receive incoming events on WebSocket")
                    .convertBodyTo(Event.class)
                    .to(EventService.INCOMING_EVENT_QUEUE);

                // Receive events on the local bus and publish them on the websocket
                from(EventService.OUTGOING_EVENT_QUEUE)
                    .routeId("Send outgoing events to WebSocket")
                    .log(LoggingLevel.DEBUG, LOG, "Sending to all websocket clients: '${body}'")
                    .to("uws://" + WEBSOCKET_EVENTS + "?sendToAll=true");
            }
        });
    }
}
