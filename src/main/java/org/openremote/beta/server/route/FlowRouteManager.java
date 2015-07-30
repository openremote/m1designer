package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class FlowRouteManager extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FlowRouteManager.class);

    public static final String DESTINATION_SINK_ID = "DESTINATION_SINK_ID";

    protected final Flow flow;
    protected List<NodeRouteManager> nodeRouteManagers = new ArrayList<>();

    public FlowRouteManager(CamelContext context, Flow flow) {
        super(context);
        this.flow = flow;
        LOG.debug("Creating builder for: " + flow);

        for (Node node : flow.getNodes()) {
            LOG.debug("Creating builder for: " + node);
            NodeRouteManager nodeRouteManager;
            switch (node.getType()) {
                case "Consumer":
                    nodeRouteManager = new ConsumerNodeRouteManager(context, flow, node);
                    break;
                case "Producer":
                    nodeRouteManager = new ProducerNodeRouteManager(context, flow, node);
                    break;
                case "Function":
                    nodeRouteManager = new FunctionNodeRouteManager(context, flow, node);
                    break;
                case "Change":
                    nodeRouteManager = new ChangeNodeRouteManager(context, flow, node);
                    break;
                case "Storage":
                    nodeRouteManager = new StorageNodeRouteManager(context, flow, node);
                    break;
                default:
                    throw new UnsupportedOperationException("Can't build route for type of node: " + node);
            }
            nodeRouteManagers.add(nodeRouteManager);
        }
    }

    @Override
    public void configure() throws Exception {
        LOG.debug("Configure routes: " + flow);

        from("direct:" + flow.getId())
            .routeId(flow.toString())
            .autoStartup(false)
            .recipientList(simple("direct:${header." + DESTINATION_SINK_ID + "}"))
            .id(flow.toString() + "###toSink");
    }

    public void addRoutesToCamelContext() throws Exception {
        addRoutesToCamelContext(getContext());
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        LOG.debug("Adding routes: " + flow);
        super.addRoutesToCamelContext(context);
        for (NodeRouteManager nodeRouteManager : nodeRouteManagers) {
            nodeRouteManager.addRoutesToCamelContext(nodeRouteManager.getContext());
        }
    }

    public void startRoutes() throws Exception {
        log.debug("Starting routes: " + flow);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().startRoute(routeDefinition.getId());
        }
        for (NodeRouteManager nodeRouteManager : nodeRouteManagers) {
            nodeRouteManager.startRoutes();
        }
    }

    public void removeRoutesFromCamelContext() throws Exception {
        LOG.debug("Removing routes: " + flow);
        RoutesDefinition routesDefinition = getRouteCollection();
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            getContext().removeRouteDefinition(routeDefinition);
        }
        for (NodeRouteManager nodeRouteManager : nodeRouteManagers) {
            nodeRouteManager.removeRoutesFromCamelContext();
        }
    }
}
