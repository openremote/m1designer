package org.openremote.beta.server.route;

import org.apache.camel.RecipientList;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;

import java.util.ArrayList;
import java.util.List;

public class WiringRouter {

    protected final Flow flow;
    protected final Node node;

    public WiringRouter(Flow flow, Node node) {
        this.flow = flow;
        this.node = node;
    }

    @RecipientList
    public List<String> getSinks() {
        List<String> sinks = new ArrayList<>();
        Slot[] sourceSlots = node.findSlots(Slot.Type.SOURCE);
        for (Slot sourceSlot : sourceSlots) {
            // Find destination wires
            Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getId());
            for (Wire sourceWire : sourceWires) {
                sinks.add("direct:" + sourceWire.getSinkId());
            }
        }
        return sinks;
    }
}