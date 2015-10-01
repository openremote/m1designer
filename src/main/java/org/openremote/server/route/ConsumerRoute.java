package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.catalog.VirtualNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.List;

public class ConsumerRoute extends NodeRoute {

    public static class Descriptor extends VirtualNodeDescriptor {

        @Override
        public String getType() {
            return Node.TYPE_CONSUMER;
        }

        @Override
        public String getTypeLabel() {
            return Node.TYPE_CONSUMER_LABEL;
        }

        @Override
        public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
            return new ConsumerRoute(context, flow, node);
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
            slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK), false));
            slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE)));
        }
    }

    public ConsumerRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        // Nothing to do
    }
}
