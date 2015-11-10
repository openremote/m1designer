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

package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteManagementUtil {

    protected static class RouteUpdater extends Thread {

        private static final Logger LOG = LoggerFactory.getLogger(RouteUpdater.class);

        final protected CamelContext context;
        final protected RouteDefinition routeDefinition;
        final protected boolean startRoute;

        public RouteUpdater(CamelContext context, RouteDefinition routeDefinition, boolean startRoute) {
            this.context = context;
            this.routeDefinition = routeDefinition;
            this.startRoute = startRoute;
        }

        @Override
        public void run() {
            try {
                String routeId = routeDefinition.getId();
                if (context.getRoute(routeId) != null) {
                    LOG.debug("Stopping and removing route: " + routeId);
                    context.stopRoute(routeId);
                    context.removeRoute(routeId);
                }
                ;
                if (startRoute) {
                    LOG.info("Adding and starting route: " + routeId);
                    context.addRouteDefinition(routeDefinition);
                    context.startRoute(routeId);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void updateRoute(CamelContext context, RouteDefinition routeDefinition, boolean startRoute) {
        new RouteUpdater(context, routeDefinition, startRoute).start();
    }
}
