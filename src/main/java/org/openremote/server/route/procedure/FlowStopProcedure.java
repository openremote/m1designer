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

package org.openremote.server.route.procedure;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.server.route.FlowRoutes;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;

import static org.openremote.shared.event.FlowDeploymentPhase.REMOVING_ROUTES;
import static org.openremote.shared.event.FlowDeploymentPhase.STOPPING;

public class FlowStopProcedure extends FlowProcedure {

    public FlowStopProcedure(CamelContext context, Flow flow) {
        super(context, flow);
    }

    public void execute(FlowRoutes flowRoutes) throws FlowProcedureException {
        stopAndRemoveRoutes(flowRoutes);
    }

    protected void stopAndRemoveRoutes(FlowRoutes flowRoutes) throws FlowProcedureException {
        clearProcessed();
        for (NodeRoute nodeRoute : flowRoutes.getNodeRoutes()) {
            RoutesDefinition routesDefinition = nodeRoute.getRouteCollection();
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                try {
                    context.stopRoute(routeDefinition.getId());
                } catch (Exception ex) {
                    throw new FlowProcedureException(ex, STOPPING, nodeRoute.getFlow(), nodeRoute.getNode(), getUnprocessedNodes());
                }
                try {
                    context.removeRoute(routeDefinition.getId());
                } catch (Exception ex) {
                    throw new FlowProcedureException(ex, REMOVING_ROUTES, nodeRoute.getFlow(), nodeRoute.getNode(), getUnprocessedNodes());
                }
            }
            addProcessed(nodeRoute.getNode());
        }
    }
}
