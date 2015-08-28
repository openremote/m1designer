package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;
import org.openremote.beta.server.route.RouteConstants;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;

public class SinkSlotPosition extends NodePredicate{

    final protected int position;

    public SinkSlotPosition(Node node, int position) {
        super(node);
        this.position = position;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Slot sink = getNode().findSlotByPosition(position, Slot.TYPE_SINK);
        if (sink == null)
            return false;
        String exchangeSlotId = exchange.getIn().getHeader(RouteConstants.SLOT_ID, String.class);
        return exchangeSlotId != null && exchangeSlotId.equals(sink.getId());
    }
}
