package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.beta.server.route.RouteManagementUtil.*;


public class FlowRoute extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlowRoute.class);

    public static final String DESTINATION_SINK_ID = "DESTINATION_SINK_ID";

    protected final Flow flow;
    protected List<NodeRoute> nodeRoutes = new ArrayList<>();

    public FlowRoute(CamelContext context, Flow flow) {
        super(context);
        this.flow = flow;
        LOG.debug("Creating builder for: " + flow);

        // TODO We need a node type system
        for (Node node : flow.getNodes()) {
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
                case Node.TYPE_WIDGET:
                    nodeRoute = new WidgetRoute(context, flow, node);
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
            nodeRoutes.add(nodeRoute);
        }
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + flow);

        from("direct:" + flow.getIdentifier().getId())
            .routeId(getRouteId(flow))
            .routeDescription(getRouteDescription(flow))
            .autoStartup(false)
            .log(LoggingLevel.DEBUG, LOG, "Flow processing: " + getRouteDescription(flow))
            .recipientList(simple("direct:${header." + DESTINATION_SINK_ID + "}"))
            .id(getProcessorId(flow, "toSink"));
    }

    public void addRoutesToCamelContext() throws Exception {
        addRoutesToCamelContext(getContext());
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        LOG.debug("Adding routes: " + flow);
        super.addRoutesToCamelContext(context);
        for (NodeRoute nodeRoute : nodeRoutes) {
            nodeRoute.addRoutesToCamelContext(nodeRoute.getContext());
        }
    }

    public void startRoutes() throws Exception {
        log.debug("Starting routes: " + flow);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().startRoute(routeDefinition.getId());
        }
        for (NodeRoute nodeRoute : nodeRoutes) {
            nodeRoute.startRoutes();
        }
    }

    public void removeRoutesFromCamelContext() throws Exception {
        LOG.debug("Removing routes: " + flow);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().removeRouteDefinition(routeDefinition);
        }
        for (NodeRoute nodeRoute : nodeRoutes) {
            nodeRoute.removeRoutesFromCamelContext();
        }
    }
}
