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
import org.apache.camel.component.bean.AmbiguousMethodCallException;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.flow.FlowService;
import org.openremote.server.route.RouteManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(EventServiceConfiguration.class);

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        if (context.hasService(RouteManagementService.class) == null)
            throw new RuntimeException("Required service missing, check Configuration order: " + RouteManagementService.class.getName());

        if (context.hasService(FlowService.class) == null)
            throw new RuntimeException("Required service missing, check Configuration order: " + FlowService.class.getName());

        EventService eventService = new EventService(
            context,
            context.hasService(FlowService.class),
            context.hasService(RouteManagementService.class)
        );

        context.addService(eventService);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(EventService.INCOMING_EVENT_QUEUE)
                    .routeId("Handle incoming events")
                    .doTry()
                    .bean(getContext().hasService(EventService.class), "onEvent")
                    .doCatch(AmbiguousMethodCallException.class) // No overloaded method for given event subtype
                    .log(LoggingLevel.DEBUG, LOG, "Ignoring unhandled event: ${body}")
                    .stop()
                    .endDoTry()
                ;
            }
        });
    }
}
