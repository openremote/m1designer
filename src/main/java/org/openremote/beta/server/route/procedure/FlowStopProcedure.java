package org.openremote.beta.server.route.procedure;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.server.route.FlowRoutes;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.REMOVING_ROUTES;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.STOPPING;

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
