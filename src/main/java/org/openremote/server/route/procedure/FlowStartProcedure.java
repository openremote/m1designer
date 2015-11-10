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
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.route.FlowRoutes;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

import static org.openremote.shared.event.FlowDeploymentPhase.*;

public class FlowStartProcedure extends FlowProcedure {

    public FlowStartProcedure(CamelContext context, Flow flow) {
        super(context, flow);
    }

    public FlowRoutes execute() throws FlowProcedureException {
        FlowRoutes flowRoutes = new FlowRoutes(getFlow());
        initRoutes(flowRoutes);
        addRoutesToContext(flowRoutes);
        startRoutes(flowRoutes);
        return flowRoutes;
    }

    protected void initRoutes(FlowRoutes flowRoutes) throws FlowProcedureException {
        for (Node node : getFlow().getNodes()) {
            try {
                clearProcessed();
                NodeRoute nodeRoute;
                NodeDescriptor nodeDescriptor =
                    (NodeDescriptor) getContext().getRegistry().lookupByName(node.getType());
                if (nodeDescriptor == null) {
                    throw new UnsupportedOperationException("No descriptor for type of node: " + node);
                }
                nodeRoute = nodeDescriptor.createRoute(getContext(), getFlow(), node);
                flowRoutes.getNodeRoutes().add(nodeRoute);
                addProcessed(node);
            } catch (Exception ex) {
                throw new FlowProcedureException(ex, INITIALIZING_NODES, flow, node, getUnprocessedNodes());
            }
        }
    }

    protected void addRoutesToContext(FlowRoutes flowRoutes) throws FlowProcedureException {
        clearProcessed();
        for (NodeRoute nodeRoute : flowRoutes.getNodeRoutes()) {
            try {
                nodeRoute.addRoutesToCamelContext(context);
                addProcessed(nodeRoute.getNode());
            } catch (Exception ex) {
                throw new FlowProcedureException(ex, ADDING_ROUTES, nodeRoute.getFlow(), nodeRoute.getNode(), getUnprocessedNodes());
            }
        }
    }

    protected void startRoutes(FlowRoutes flowRoutes) throws FlowProcedureException {
        clearProcessed();
        for (NodeRoute nodeRoute : flowRoutes.getNodeRoutes()) {
            RoutesDefinition routesDefinition = nodeRoute.getRouteCollection();
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                try {
                    context.startRoute(routeDefinition.getId());
                    addProcessed(nodeRoute.getNode());
                } catch (Exception ex) {
                    throw new FlowProcedureException(ex, STARTING, nodeRoute.getFlow(), nodeRoute.getNode(), getUnprocessedNodes());
                }
            }
        }
    }
}
