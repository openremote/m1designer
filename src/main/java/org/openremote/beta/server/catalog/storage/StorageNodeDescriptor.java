package org.openremote.beta.server.catalog.storage;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

import java.util.List;

public class StorageNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:openremote:flow:node:storage";
    public static final String TYPE_LABEL = "Storage";

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
        return new StorageRoute(context, flow, node);
    }

    @Override
    public void addSlots(List<Slot> slots) {
        super.addSlots(slots);
        slots.add(new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)));
    }

}
