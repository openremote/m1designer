package org.openremote.server.inventory.discovery;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
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
                .bean(getContext().hasService(AdapterDiscoveryService.class), "getAdapters")
                .endRest()

                .get("{id}")
                .route().id("GET discovered adapter by ID")
                .bean(getContext().hasService(AdapterDiscoveryService.class), "getAdapter")
                .to("direct:restStatusNotFound")
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
                })
                .bean(getContext().hasService(InboxService.class), "getDiscoveredDevices")
                .endRest()

                .get("/adapter")
                .route().id("GET adapters of discovery inbox")
                .bean(getContext().hasService(InboxService.class), "getAdapters")
                .endRest()

                .post("/adapter")
                .consumes("application/json")
                .type(Adapter.class)
                .route().id("POST adapter into discovery inbox")
                .process(exchange -> {
                    Adapter adapter = exchange.getIn().getBody(Adapter.class);

                    getContext().hasService(InboxService.class).addAdapter(adapter);
                    getContext().hasService(InboxService.class).triggerDiscovery(adapter);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 201);

                    exchange.getOut().setHeader(
                        "Location",
                        url(exchange, REST_SERVICE_CONTEXT_PATH, "discovery", "inbox", "adapter", adapter.getId())
                    );
                })
                .endRest()

                .delete("/adapter/{adapterId}")
                .route().id("DELETE adapter from discovery inbox")
                .bean(getContext().hasService(InboxService.class), "removeAdapter")
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        AdapterDiscoveryService adapterDiscoveryService = new AdapterDiscoveryService(context);
        context.addService(adapterDiscoveryService);

        InboxService inboxService = new InboxService(adapterDiscoveryService);
        context.addService(inboxService);

        context.addRoutes(
            new DiscoveryServiceRouteBuilder(
                Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))
            )
        );
    }

}
