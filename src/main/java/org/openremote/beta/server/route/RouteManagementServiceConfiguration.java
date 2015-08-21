package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;

public class RouteManagementServiceConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        RouteManagementService routeManagementService = new RouteManagementService(context);
        context.addService(routeManagementService);
    }

}
