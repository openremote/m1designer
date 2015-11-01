package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.catalog.VirtualNodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.openremote.server.route.RouteConstants.SLOT_ID;

public class ProducerRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerRoute.class);

    public static class Descriptor extends VirtualNodeDescriptor {

        @Override
        public String getType() {
            return Node.TYPE_PRODUCER;
        }

        @Override
        public String getTypeLabel() {
            return Node.TYPE_PRODUCER_LABEL;
        }

        @Override
        public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
            return new ProducerRoute(context, flow, node);
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
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK));
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, false));
        }
    }

    public ProducerRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        // Nothing to do
    }

    @Override
    protected void configureDestination(ProcessorDefinition routeDefinition) throws Exception {
        // Many (or no) internal (subflow) consumers receive this message asynchronously
        routeDefinition
            .process(exchange -> {
                LOG.debug("Handing off exchange to asynchronous queue: " + getNode());

                Slot[] sourceSlots = new Slot[0];
                if (isSinkRouting().matches(exchange)) {
                    // Send message to all source slots
                    sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                } else {
                    // Send message to a single source slot
                    sourceSlots = new Slot[]{getNode().findSlot(getSlotId(exchange))};
                }
                exchange.getIn().removeHeader(SLOT_ID);
                LOG.debug("Using wires of destination source slots: " + Arrays.toString(sourceSlots));

                ProducerTemplate producerTemplate = getContext().createProducerTemplate();

                for (Slot sourceSlot : sourceSlots) {
                    Exchange exchangeCopy = copyExchange(exchange, false);
                    LOG.debug("Sending message to asynchronous queue: " + sourceSlot.getId());
                    producerTemplate.send(
                        "seda:" + sourceSlot.getId() + "?multipleConsumers=true&waitForTaskToComplete=NEVER",
                        exchangeCopy
                    );
                }
            })
            .id(getProcessorId("toQueues"));
    }
}
