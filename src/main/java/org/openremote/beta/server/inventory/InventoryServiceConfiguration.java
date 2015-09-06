package org.openremote.beta.server.inventory;

import com.ait.tooling.nativetools.client.util.Client;
import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.WebserverConfiguration.RestRouteBuilder;
import org.openremote.beta.server.flow.FlowService;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

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

                .put("{name}")
                .consumes("application/json")
                .type(ClientPreset.class)
                .route().id("PUT client preset by name")
                .process(exchange -> {
                    ClientPreset clientPreset = exchange.getIn().getBody(ClientPreset.class);
                    try {
                        boolean found = getContext().hasService(InventoryService.class).putClientPreset(clientPreset);
                        if (!found) {
                            exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);
                            return;
                        }
                    } catch (Exception ex) {
                        LOG.info("Error putting client preset '" + clientPreset.getName() + "'", ex);
                        exchange.getIn().setHeader(HTTP_RESPONSE_CODE, 400);
                        return;
                    }
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                })
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
