package org.openremote.beta.server.catalog.filter;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;

public class FilterNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:org-openremote:flow:node:filter";
    public static final String TYPE_LABEL = "Filter";

    public static final String PROPERTY_ON_TRIGGER = "onTrigger";

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
        return new FilterRoute(context, flow, node);
    }

    @Override
    public Slot[] createSlots() {
        return new Slot[] {
            new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)),
            new Slot("Trigger", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK)),
            new Slot(new Identifier(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE))
        };
    }
}
