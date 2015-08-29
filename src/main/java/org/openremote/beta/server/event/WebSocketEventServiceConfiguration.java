package org.openremote.beta.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEventServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventServiceConfiguration.class);

    public static final String WEBSOCKET_EVENTS = "events";

    public static final String WEBSOCKET_ADDRESS = "WEBSOCKET_ADDRESS";
    public static final String WEBSOCKET_ADDRESS_DEFAULT = "0.0.0.0";
    public static final String WEBSOCKET_PORT = "WEBSOCKET_PORT";
    public static final String WEBSOCKET_PORT_DEFAULT = "9292";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                String host = environment.getProperty(WEBSOCKET_ADDRESS, WEBSOCKET_ADDRESS_DEFAULT);
                String port = environment.getProperty(WEBSOCKET_PORT, WEBSOCKET_PORT_DEFAULT);
                LOG.info("Configuring WebSocket server: " + host + ":" + port);

                WebsocketComponent websocketComponent = getContext().getComponent("websocket", WebsocketComponent.class);
                websocketComponent.setHost(host);
                websocketComponent.setPort(Integer.valueOf(port));

                // Receive events on the websocket and publish them on the local bus
                from("websocket://" + WEBSOCKET_EVENTS + "?maxIdleTime=30000")
                    .routeId("Receive incoming events on WebSocket")
                    .convertBodyTo(Event.class)
                    .to(EventService.INCOMING_EVENT_QUEUE);

                // Receive events on the local bus and publish them on the websocket
                from(EventService.OUTGOING_EVENT_QUEUE)
                    .routeId("Send outgoing events to WebSocket")
                    .log(LoggingLevel.DEBUG, LOG, "Sending to all websocket clients: ${body}")
                    .to("websocket://" + WEBSOCKET_EVENTS + "?sendToAll=true");
            }
        });
    }
}
