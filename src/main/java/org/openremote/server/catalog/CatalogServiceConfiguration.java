package org.openremote.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.web.WebserverConfiguration.RestRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;

public class CatalogServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceConfiguration.class);

    class CatalogServiceRouteBuilder extends RestRouteBuilder {

        public CatalogServiceRouteBuilder(boolean debug) {
            super(debug);
        }

        @Override
        public void configure() throws Exception {
            super.configure();

            rest("/catalog")

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

        context.addRoutes(
            new CatalogServiceRouteBuilder(
                Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))
            )
        );
    }

}
