package org.openremote.server.catalog.flip;

import org.apache.camel.CamelContext;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.model.Identifier;

import java.util.List;

public class FlipNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:openremote:flow:node:flip";
    public static final String TYPE_LABEL = "Flip";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTypeLabel() {
        return TYPE_LABEL;
    }

    @Override
    public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
        return new FlipRoute(context, flow, node);
    }

    @Override
    public void addSlots(List<Slot> slots) {
        super.addSlots(slots);
        slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
        slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE)));
    }
}