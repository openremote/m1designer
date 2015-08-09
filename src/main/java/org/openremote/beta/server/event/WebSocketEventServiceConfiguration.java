package org.openremote.beta.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.camel.component.websocket.WebsocketConstants;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.shared.event.FlowIdEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEventServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketEventServiceConfiguration.class);

    public static final String WEBSOCKET_FLOW_EVENTS = "flow";
    public static final String WEBSOCKET_MESSAGE_EVENTS = "message";

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
                from("websocket://" + WEBSOCKET_FLOW_EVENTS)
                    .routeId("Receive incoming flow events on WebSocket")
                    .convertBodyTo(FlowIdEvent.class)
                    .to(EventService.INCOMING_FLOW_EVENT_QUEUE);

                // Receive events on the local bus and publish them on the websocket
                from(EventService.OUTGOING_FLOW_EVENT_QUEUE)
                    .routeId("Send outgoing flow events to WebSocket")
                    .log(LoggingLevel.DEBUG, LOG, "Sending to all websocket clients: ${body}")
                    .to("websocket://" + WEBSOCKET_FLOW_EVENTS + "?sendToAll=true");

                // Receive events on the websocket and publish them on the local bus
                from("websocket://" + WEBSOCKET_MESSAGE_EVENTS)
                    .routeId("Receive incoming message events on WebSocket")
                    .convertBodyTo(MessageEvent.class)
                    .to(EventService.INCOMING_MESSAGE_EVENT_QUEUE);

                // Receive events on the local bus and publish them on the websocket
                from(EventService.OUTGOING_MESSAGE_EVENT_QUEUE)
                    .routeId("Send outgoing message events to WebSocket")
                    .log(LoggingLevel.DEBUG, LOG, "Sending to all websocket clients: ${body}")
                    .to("websocket://" + WEBSOCKET_MESSAGE_EVENTS + "?sendToAll=true");
            }
        });
    }
}
