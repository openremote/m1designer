package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.route.predicate.PropertyIsSet;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

public class ChangeRoute extends NodeRoute {

    public ChangeRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId(flow, node, "selectChange"))
                .when(new PropertyIsSet(getNode(), "prepend"))
                    .transform(body().prepend(getPropertyValue("prepend")))
                    .id(getProcessorId(flow, node, "doPrepend"))
                .endChoice()
                .when(new PropertyIsSet(getNode(), "append"))
                    .transform(body().append(getPropertyValue("append")))
                    .id(getProcessorId(flow, node, "doAppend"))
                .endChoice()
            .end();
    }
}
