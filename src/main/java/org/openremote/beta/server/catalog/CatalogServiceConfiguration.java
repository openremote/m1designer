package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.WebserverConfiguration.RestRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceConfiguration.class);

    class CatalogServiceRouteBuilder extends RestRouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/catalog")

                .get("/guid")
                .route().id("GET batch of generated GUIDs")
                .bean(getContext().hasService(CatalogService.class), "generateIdBatch")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .endRest()

                .get()
                .route().id("GET all catalog items")
                .bean(getContext().hasService(CatalogService.class), "getItems")
                .endRest()

                .get("/node/{type}")
                .route().id("GET new node by node type ")
                .bean(getContext().hasService(CatalogService.class), "getNewNode")
                .to("direct:restStatusNotFound")
                .endRest();

        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        CatalogService catalogService = new CatalogService(context);
        context.addService(catalogService);

        context.addRoutes(new CatalogServiceRouteBuilder());
    }

}
