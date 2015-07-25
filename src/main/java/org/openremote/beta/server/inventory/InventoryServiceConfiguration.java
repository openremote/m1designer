package org.openremote.beta.server.inventory;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.inventory.discovery.AdapterDiscoveryService;
import org.openremote.beta.shared.flow.Flow;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class InventoryServiceConfiguration implements Configuration {

    class InventoryServiceRouteBuilder  extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/adapter")
                .get()
                .route().id("GET all discovered adapters")
                .process(exchange -> {
                    AdapterDiscoveryService service = getContext().hasService(AdapterDiscoveryService.class);
                    exchange.getOut().setBody(service.getAdapters());
                })
                .endRest()

                .get("{id}")
                .route().id("GET discovered adapter by ID")
                .process(exchange -> {
                    AdapterDiscoveryService service = getContext().hasService(AdapterDiscoveryService.class);
                    exchange.getOut().setBody(
                        service.getAdapter(exchange.getIn().getHeader("id", String.class))
                    );
                    if (exchange.getOut().getBody() == null)
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);
                })
                .endRest();

        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        context.addService(new AdapterDiscoveryService(context));
        context.addRoutes(new InventoryServiceRouteBuilder());
    }

}
