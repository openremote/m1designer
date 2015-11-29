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

package org.openremote.server.inventory.discovery;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.inventory.InventoryService;
import org.openremote.server.web.WebserverConfiguration.RestRouteBuilder;
import org.openremote.shared.inventory.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;
import static org.openremote.shared.Constants.REST_SERVICE_CONTEXT_PATH;
import static org.openremote.server.util.UrlUtil.url;

public class DiscoveryServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServiceConfiguration.class);

    class DiscoveryServiceRouteBuilder extends RestRouteBuilder {

        public DiscoveryServiceRouteBuilder(boolean debug) {
            super(debug);
        }

        @Override
        public void configure() throws Exception {
            super.configure();

            rest("/discovery/adapter")
                .get()
                .route().id("GET all discovered adapters")
                .bean(getContext().hasService(DiscoveryService.class), "getAdapters")
                .endRest()

                .get("{id}")
                .route().id("GET discovered adapter by ID")
                .bean(getContext().hasService(DiscoveryService.class), "getAdapter")
                .to("direct:restStatusNotFound")
                .endRest()

                .put("{id}")
                .consumes("application/json")
                .type(Adapter.class)
                .route().id("PUT configured adapter by ID")
                .process(exchange -> {
                    Adapter adapter = exchange.getIn().getBody(Adapter.class);
                    boolean found = getContext().hasService(DiscoveryService.class).putAdapter(adapter);
                    exchange.getOut().setBody(null);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, found ? 204 : 404);
                })
                .endRest()

                .post("/trigger")
                .consumes("application/json")
                .type(Adapter.class)
                .route().id("POST adapter to trigger discovery")
                .process(exchange -> {
                    Adapter adapter = exchange.getIn().getBody(Adapter.class);
                    try {
                        getContext().hasService(DiscoveryService.class).triggerDiscovery(adapter);
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 201);
                        exchange.getOut().setHeader(
                            "Location",
                            url(exchange, REST_SERVICE_CONTEXT_PATH, "discovery", "device")
                        );
                    } catch (IllegalArgumentException ex) {
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 400);
                        exchange.getOut().setBody(ex.getMessage());
                    } catch (IllegalStateException ex) {
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 409);
                        exchange.getOut().setBody(ex.getMessage());
                    }
                })
                .endRest();

            rest("/discovery/device")
                .get()
                .route().id("GET all discovered devices")
                .bean(getContext().hasService(DiscoveryService.class), "getDiscoveredDevices")
                .endRest();

        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        DiscoveryService discoveryService = new DiscoveryService(context);
        context.addService(discoveryService);

        context.addRoutes(
            new DiscoveryServiceRouteBuilder(
                Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))
            )
        );
    }

}
