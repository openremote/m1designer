package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.catalog.VirtualNodeDescriptor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.openremote.beta.server.route.RouteConstants.SLOT_ID;

public class ProducerRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerRoute.class);

    public static final String NODE_TYPE = "urn:org-openremote:flow:node:producer";
    public static final String NODE_TYPE_LABEL = "Source";

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
            slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
            slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE), false));
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
