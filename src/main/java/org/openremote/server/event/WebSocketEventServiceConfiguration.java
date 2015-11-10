/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.shared.event.Event;
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
