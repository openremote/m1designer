package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
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
                };
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

    public static String getRouteId(Flow flow) {
        return flow.getIdentifier().toString();
    }

    public static String getRouteId(Flow flow, Node node) {
        return getRouteId(flow) + ";" + node.getIdentifier();
    }

    public static String getRouteId(Flow flow, Node node, Slot slot) {
        return getRouteId(flow, node) + ";" + slot.getIdentifier();
    }

    public static String getProcessorId(Flow flow, String processorLabel) {
        return processorLabel + ";" + flow.getIdentifier();
    }

    public static String getProcessorId(Flow flow, Node node, String processorLabel) {
        return processorLabel + ";" + flow.getIdentifier() + ";" + node.getIdentifier();
    }

    public static String getProcessorId(Flow flow, Node node, Slot slot, String processorLabel) {
        return processorLabel + ";" + flow.getIdentifier() + ";" + node.getIdentifier() + ";" + slot.getIdentifier();
    }

    public static String getRouteDescription(Flow flow) {
        return flow.toString();
    }

    public static String getRouteDescription(Flow flow, Node node) {
        return getRouteDescription(flow)  + ";" + node;
    }

    public static String getRouteDescription(Flow flow, Node node, Slot slot) {
        return getRouteDescription(flow, node)  + ";" + slot;
    }

}
