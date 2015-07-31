package org.openremote.beta.server.route;

import org.apache.camel.RecipientList;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WiringRouter {

    private static final Logger LOG = LoggerFactory.getLogger(WiringRouter.class);

    protected final Flow flow;
    protected final Node node;

    public WiringRouter(Flow flow, Node node) {
        this.flow = flow;
        this.node = node;
    }

    @RecipientList
    public List<String> getSinks() {
        List<String> sinks = new ArrayList<>();
        Slot[] sourceSlots = node.findSlots(Slot.TYPE_SOURCE);

        switch(node.getIdentifier().getType()) {
            case Node.TYPE_PRODUCER:
                // If it's a producer, it has an invisible source slot, where many (or no) consumers receive asynchronously
                LOG.debug("Producer node, wiring source slots to asynchronous queues: " + node);
                for (Slot sourceSlot : sourceSlots) {
                    LOG.debug("Adding asynchronous queue for: " + sourceSlot);
                    sinks.add("seda:" + sourceSlot.getIdentifier().getId() + "?multipleConsumers=true&failIfNoConsumers=false&waitForTaskToComplete=NEVER");
                }
                break;
            case Node.TYPE_SUBFLOW:
                // If it's a subflow, it does its own internal routing of its source wires
                LOG.debug("Subflow node, internal wiring");
                break;
            default:
                LOG.debug("Regular node, wiring source slots directly: " + node);
                for (Slot sourceSlot : sourceSlots) {
                    LOG.debug("Finding wires of: " + sourceSlot);
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getIdentifier().getId());
                    for (Wire sourceWire : sourceWires) {
                        LOG.debug("Adding direct sink destination: " + sourceWire.getSinkId());
                        sinks.add("direct:" + sourceWire.getSinkId());
                    }
                }
                break;
        }

        return sinks;
    }
}