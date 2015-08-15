package org.openremote.beta.server.catalog.filter;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class FilterNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:org-openremote:flow:node:filter";
    public static final String TYPE_LABEL = "Filter";

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




}
