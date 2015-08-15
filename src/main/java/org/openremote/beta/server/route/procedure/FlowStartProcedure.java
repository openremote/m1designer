package org.openremote.beta.server.route.procedure;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.FlowRoutes;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

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
                    (NodeDescriptor) getContext().getRegistry().lookupByName(node.getIdentifier().getType());
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
