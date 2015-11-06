package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.NodeColor;
import org.openremote.shared.flow.Slot;

import java.util.List;

public class SensorRoute extends NodeRoute {

    public static final String NODE_TYPE = "urn:openremote:flow:node:sensor";
    public static final String NODE_TYPE_LABEL = "Sensor";

    public static class Descriptor extends NodeDescriptor {

        @Override
        public CatalogCategory getCatalogCategory() {
            return null;
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
            return new SensorRoute(context, flow, node);
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
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE));
        }
    }

    public SensorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        // Nothing to do
    }
}
