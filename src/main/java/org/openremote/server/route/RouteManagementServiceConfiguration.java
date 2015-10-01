package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;

public class RouteManagementServiceConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        RouteManagementService routeManagementService = new RouteManagementService(context);
        context.addService(routeManagementService);
    }

}
