package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class ActuatorRoute extends NodeRoute {

    public static final String NODE_TYPE = "urn:openremote:flow:node:actuator";
    public static final String NODE_TYPE_LABEL = "Actuator";

    public static class Descriptor extends NodeDescriptor {

        @Override
        public boolean isInternal() {
            return true;
        }

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
            return new ActuatorRoute(context, flow, node);
        }

        @Override
        public NodeColor getColor() {
            return NodeColor.SENSOR_ACTUATOR;
        }

        @Override
        public Node initialize(Node node) {
            node = super.initialize(node);
            node.setClientAccess(true);
            return node;
        }

        @Override
        public void addSlots(List<Slot> slots) {
            super.addSlots(slots);
            slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
        }
    }

    public ActuatorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        // Nothing to do
    }
}
