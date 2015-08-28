package org.openremote.beta.server.catalog.change;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class ChangeRoute extends NodeRoute {

    public ChangeRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        if (getNodeProperties().has("prepend")) {
            routeDefinition
                .transform(body().prepend(getNodeProperties().get("prepend").asText()))
                .id(getProcessorId("doPrepend"));
        }

        if (getNodeProperties().has("append")) {
            routeDefinition
                .transform(body().append(getNodeProperties().get("append").asText()))
                .id(getProcessorId("doAppend"));
        }
    }
}
