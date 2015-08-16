package org.openremote.beta.server.catalog.change;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.predicate.PropertyIsSet;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Properties;

public class ChangeRoute extends NodeRoute {

    public ChangeRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId("selectChange"))
                .when(new PropertyIsSet(getNode(), "prepend"))
                    .transform(body().prepend(Properties.get(getNode().getProperties(), "prepend")))
                    .id(getProcessorId("doPrepend"))
                .endChoice()
                .when(new PropertyIsSet(getNode(), "append"))
                    .transform(body().append(Properties.get(getNode().getProperties(), "append")))
                    .id(getProcessorId("doAppend"))
                .endChoice()
            .end();
    }
}
