package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.catalog.VirtualNodeDescriptor;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;

public class ConsumerRoute extends NodeRoute {

    public static final String NODE_TYPE = "urn:org-openremote:flow:node:consumer";
    public static final String NODE_TYPE_LABEL = "Sink";

    public static class Descriptor extends VirtualNodeDescriptor {

        @Override
        public String getType() {
            return NODE_TYPE;
        }

        @Override
        public String getTypeLabel() {
            return NODE_TYPE_LABEL;
        }

        @Override
        public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
            return new ConsumerRoute(context, flow, node);
        }

        @Override
        public NodeColor getColor() {
            return NodeColor.VIRTUAL;
        }

        @Override
        public boolean isClientAccessEnabled() {
            return true;
        }
    }

    public ConsumerRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        // Nothing to do
    }

    @Override
    protected boolean isPublishingMessageEvents() {
        return true;
    }
}
