package org.openremote.beta.server.catalog.change;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class ChangeNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:org-openremote:flow:node:change";
    public static final String TYPE_LABEL = "Change";

    public static final String PROPERTY_PREPEND = "prepend";
    public static final String PROPERTY_APPEND = "append";

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
        return new ChangeRoute(context, flow, node);
    }




}
