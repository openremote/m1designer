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

package org.openremote.server.inventory;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.web.WebserverConfiguration.RestRouteBuilder;
import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;
import static org.openremote.shared.Constants.REST_SERVICE_CONTEXT_PATH;
import static org.openremote.server.util.UrlUtil.url;

public class InventoryServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceConfiguration.class);

    class InventoryServiceRouteBuilder extends RestRouteBuilder {

        public InventoryServiceRouteBuilder(boolean debug) {
            super(debug);
        }

        @Override
        public void configure() throws Exception {
            super.configure();

            rest("/inventory/device")
                .get()
                .route().id("GET all devices")
                .bean(getContext().hasService(DeviceService.class), "getDevices")
                .endRest()

                .get("{id}")
                .route().id("GET device by ID")
                .bean(getContext().hasService(DeviceService.class), "getDevice")
                .to("direct:restStatusNotFound")
                .endRest()

                .post()
                .consumes("application/json")
                .type(Device.class)
                .route().id("POST new device")
                .process(exchange -> {
                    Device device = exchange.getIn().getBody(Device.class);
                    String id = getContext().hasService(DeviceService.class).postDevice(device);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 201);
                    exchange.getOut().setHeader("Location", url(exchange, REST_SERVICE_CONTEXT_PATH, "inventory", "device", id));
                })
                .endRest()

                .put("{id}")
                .consumes("application/json")
                .type(Device.class)
                .route().id("PUT device by ID")
                .process(exchange -> {
                    String id = exchange.getIn().getHeader("id", String.class);
                    Device device = exchange.getIn().getBody(Device.class);
                    boolean found = getContext().hasService(DeviceService.class).putDevice(id, device);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, found ? 204 : 404);
                })
                .endRest()

                .delete("{id}")
                .route().id("DELETE device by ID")
                .bean(getContext().hasService(DeviceService.class), "deleteDevice")
                .endRest();

            rest("/inventory/preset")
                .get()
                .route().id("GET all client presets")
                .bean(getContext().hasService(ClientPresetService.class), "getClientPresets")
                .endRest()

                .get("{id}")
                .route().id("GET client preset by ID")
                .bean(getContext().hasService(ClientPresetService.class), "getClientPreset")
                .to("direct:restStatusNotFound")
                .endRest()

                .post()
                .consumes("application/json")
                .type(ClientPreset.class)
                .route().id("POST new client preset")
                .process(exchange -> {
                    ClientPreset clientPreset = exchange.getIn().getBody(ClientPreset.class);
                    Long id = getContext().hasService(ClientPresetService.class).postClientPreset(clientPreset);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 201);
                    exchange.getOut().setHeader("Location", url(exchange, REST_SERVICE_CONTEXT_PATH, "inventory", "preset", id.toString()));
                })
                .endRest()

                .put("{id}")
                .consumes("application/json")
                .type(ClientPreset.class)
                .route().id("PUT client preset by ID")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    ClientPreset clientPreset = exchange.getIn().getBody(ClientPreset.class);
                    boolean found = getContext().hasService(ClientPresetService.class).putClientPreset(id, clientPreset);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, found ? 204 : 404);
                })
                .endRest()

                .delete("{id}")
                .route().id("DELETE client preset by ID")
                .bean(getContext().hasService(ClientPresetService.class), "deleteClientPreset")
                .endRest();

        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        DeviceService deviceService = new DeviceService(context);
        context.addService(deviceService);

        ClientPresetService clientPresetService = new ClientPresetService(context);
        context.addService(clientPresetService);

        context.addRoutes(
            new InventoryServiceRouteBuilder(
                Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))
            )
        );
    }

}
