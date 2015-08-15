package org.openremote.beta.server.catalog.storage;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.catalog.function.FunctionRoute;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class StorageNodeDescriptor extends NodeDescriptor {

    public static final String TYPE = "urn:org-openremote:flow:node:storage";
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

}
