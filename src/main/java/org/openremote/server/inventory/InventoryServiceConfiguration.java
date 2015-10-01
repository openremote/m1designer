package org.openremote.server.inventory;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.web.WebserverConfiguration.RestRouteBuilder;
import org.openremote.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.shared.Constants.REST_SERVICE_CONTEXT_PATH;
import static org.openremote.server.util.UrlUtil.url;

public class InventoryServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceConfiguration.class);

    class InventoryServiceRouteBuilder extends RestRouteBuilder {

        @Override
        public void configure() throws Exception {
            super.configure();

            rest("/inventory/preset")
                .get()
                .route().id("GET all client presets")
                .bean(getContext().hasService(InventoryService.class), "getClientPresets")
                .endRest()

                .get("{id}")
                .route().id("GET client preset by ID")
                .bean(getContext().hasService(InventoryService.class), "getClientPreset")
                .to("direct:restStatusNotFound")
                .endRest()

                .post()
                .consumes("application/json")
                .type(ClientPreset.class)
                .route().id("POST new client preset")
                .process(exchange -> {
                    ClientPreset clientPreset = exchange.getIn().getBody(ClientPreset.class);
                    Long id = getContext().hasService(InventoryService.class).postClientPreset(clientPreset);
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
                    boolean found = getContext().hasService(InventoryService.class).putClientPreset(id, clientPreset);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, found ? 204 : 404);
                })
                .endRest()

                .delete("{id}")
                .route().id("DELETE client preset by ID")
                .bean(getContext().hasService(InventoryService.class), "deleteClientPreset")
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        InventoryService service = new InventoryService();
        context.addService(service);

        context.addRoutes(new InventoryServiceRouteBuilder());
    }

}
