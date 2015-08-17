package org.openremote.beta.server.catalog.change;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.predicate.PropertyIsSet;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Properties;

import static org.openremote.beta.server.catalog.change.ChangeNodeDescriptor.PROPERTY_APPEND;
import static org.openremote.beta.server.catalog.change.ChangeNodeDescriptor.PROPERTY_PREPEND;

public class ChangeRoute extends NodeRoute {

    public ChangeRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId("selectChange"))
                .when(new PropertyIsSet(getNode(), PROPERTY_PREPEND))
                    .transform(body().prepend(Properties.get(getNode().getProperties(), PROPERTY_PREPEND)))
                    .id(getProcessorId("doPrepend"))
                .endChoice()
                .when(new PropertyIsSet(getNode(), PROPERTY_APPEND))
                    .transform(body().append(Properties.get(getNode().getProperties(), PROPERTY_APPEND)))
                    .id(getProcessorId("doAppend"))
                .endChoice()
            .end();
    }
}
