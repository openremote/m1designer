package org.openremote.server.catalog.flip;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

import java.util.Locale;

public class FlipRoute extends NodeRoute {

    public FlipRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        routeDefinition
            .process(exchange -> {
                String result = null;
                String input = getInput(exchange);
                if (input.toLowerCase(Locale.ROOT).equals("true")) {
                    result = "false";
                } else if (input.toLowerCase(Locale.ROOT).equals("false")) {
                    result = "true";
                } else {
                    try {
                        int inputInt = Integer.valueOf(input);
                        if (inputInt == 0) {
                            result = "1";
                        } else if (inputInt == 1) {
                            result = "0";
                        }
                    } catch (Exception ex) {
                        // Not a number
                    }
                }
                if (result != null) {
                    exchange.getIn().setBody(result);
                }
            })
            .id(getProcessorId("doFlip"));

    }
}
