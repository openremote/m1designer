package org.openremote.beta.server.inventory.discovery;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.shared.inventory.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class DiscoveryServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryServiceConfiguration.class);

    class DiscoveryServiceRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/discovery/adapter")
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

            rest("/discovery/inbox")
                .get()
                .route().id("GET all discovered devices")
                .process(exchange -> {
                    InboxService inboxService = getContext().hasService(InboxService.class);
                    boolean refresh = exchange.getIn().getHeader("refresh", boolean.class);
                    if (refresh) {
                        inboxService.triggerDiscovery();
                    }
                    exchange.getOut().setBody(inboxService.getDiscoveredDevices());
                })
                .endRest()

                .get("/adapter")
                .route().id("GET adapters of discovery inbox")
                .process(exchange -> {
                    InboxService inboxService = getContext().hasService(InboxService.class);
                    exchange.getOut().setBody(inboxService.getAdapters());
                })
                .endRest()

                .post("/adapter")
                .type(Adapter.class)
                .consumes("application/json")
                .route().id("POST adapter into discovery inbox")
                .onException(JsonProcessingException.class)
                .to("log:org.openremote.beta.json?level=WARN&showCaughtException=true&showBodyType=false&showExchangePattern=false")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(constant(null))
                .handled(true)
                .end()
                .process(exchange -> {
                    Adapter adapter = exchange.getIn().getBody(Adapter.class);
                    if (adapter == null) {
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);
                        return;
                    }
                    getContext().hasService(InboxService.class).addAdapter(adapter);
                    getContext().hasService(InboxService.class).triggerDiscovery(adapter);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 201);
                    exchange.getOut().setHeader("Location", "/discovery/inbox/adapter/" + adapter.getIdentifier().getId());
                })
                .endRest()

                .delete("/adapter/{adapterId}")
                .route().id("DELETE adapter from discovery inbox")
                .process(exchange -> {
                    String adapterId = exchange.getIn().getHeader("adapterId", String.class);
                    getContext().hasService(InboxService.class).removeAdapter(adapterId);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 200);
                })
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        AdapterDiscoveryService adapterDiscoveryService = new AdapterDiscoveryService(context);
        context.addService(adapterDiscoveryService);

        InboxService inboxService = new InboxService(adapterDiscoveryService);
        context.addService(inboxService);

        context.addRoutes(new DiscoveryServiceRouteBuilder());
    }

}
