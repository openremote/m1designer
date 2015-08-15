package org.openremote.beta.server.catalog.widget;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.catalog.ClientNodeDescriptor;
import org.openremote.beta.server.route.ClientRoute;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class PushButtonNodeDescriptor extends ClientNodeDescriptor {

    public static final String TYPE = "urn:org-openremote:widget:pushbutton";
    public static final String TYPE_LABEL = "Push Button";

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
        return new ClientRoute(context, flow, node);
    }
}
