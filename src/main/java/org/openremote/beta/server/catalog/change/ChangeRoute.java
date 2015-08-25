package org.openremote.beta.server.catalog.change;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class ChangeRoute extends NodeRoute<ChangeProperties> {

    public ChangeRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node, ChangeProperties.class);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {

        if (getNodeProperties() != null && getNodeProperties().getPrepend() != null) {
            routeDefinition
                .transform(body().prepend(getNodeProperties().getPrepend()))
                .id(getProcessorId("doPrepend"));
        }

        if (getNodeProperties() != null && getNodeProperties().getAppend() != null) {
            routeDefinition
                .transform(body().append(getNodeProperties().getAppend()))
                .id(getProcessorId("doAppend"));
        }
    }
}
