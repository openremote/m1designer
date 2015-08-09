package org.openremote.beta.server.route.procedure;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.server.route.*;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

public class FlowStartProcedure extends FlowProcedure {

    public FlowStartProcedure(CamelContext context, Flow flow) {
        super(context, flow);
    }

    public FlowRoutes execute() throws FlowProcedureException {
        FlowRoutes flowRoutes = new FlowRoutes(flow);
        initRoutes(flowRoutes);
        addRoutesToContext(flowRoutes);
        startRoutes(flowRoutes);
        return flowRoutes;
    }

    protected void initRoutes(FlowRoutes flowRoutes) throws FlowProcedureException {
        // TODO We need a node type system
        for (Node node : flow.getNodes()) {
            try {
                clearProcessed();
                NodeRoute nodeRoute;
                switch (node.getIdentifier().getType()) {
                    case Node.TYPE_CONSUMER:
                        nodeRoute = new ConsumerRoute(context, flow, node);
                        break;
                    case Node.TYPE_PRODUCER:
                        nodeRoute = new ProducerRoute(context, flow, node);
                        break;
                    case Node.TYPE_SENSOR:
                        nodeRoute = new SensorRoute(context, flow, node);
                        break;
                    case Node.TYPE_ACTUATOR:
                        nodeRoute = new ActuatorRoute(context, flow, node);
                        break;
                    case Node.TYPE_CLIENT:
                        nodeRoute = new ClientRoute(context, flow, node);
                        break;
                    case Node.TYPE_FUNCTION:
                        nodeRoute = new FunctionRoute(context, flow, node);
                        break;
                    case Node.TYPE_FILTER:
                        nodeRoute = new FilterRoute(context, flow, node);
                        break;
                    case Node.TYPE_CHANGE:
                        nodeRoute = new ChangeRoute(context, flow, node);
                        break;
                    case Node.TYPE_STORAGE:
                        nodeRoute = new StorageRoute(context, flow, node);
                        break;
                    case Node.TYPE_SUBFLOW:
                        nodeRoute = new SubflowRoute(context, flow, node);
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't build route for type of node: " + node);
                }
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
