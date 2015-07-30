package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.script.ScriptBuilder;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.server.util.JsonUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.builder.script.ScriptBuilder.javaScript;
import static org.openremote.beta.shared.util.Util.*;

public class FunctionNodeRouteManager extends NodeRouteManager {

    public FunctionNodeRouteManager(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configure(RouteDefinition routeDefinition) throws Exception {
        if (node.hasProperties()) {

            String javascriptlet = getString(getMap(node.getProperties()), "javascript");
            if (javascriptlet != null && javascriptlet.length() > 0) {
                routeDefinition
                    .process(exchange -> {
                        Map<String, Object> arguments = new HashMap<>();
                        // TODO Input type conversion through properties
                        arguments.put("input", JsonUtil.JSON.readValue(exchange.getIn().getBody(String.class), Object.class));
                        arguments.put("output", new HashMap<String, Object>());
                        exchange.getIn().setHeader(ScriptBuilder.ARGUMENTS, arguments);
                    })
                    .id(flow.toString() + "###" + node.toString() + "###prepareJavascript")
                    .transform(javaScript(javascriptlet))
                    .id(flow.toString() + "###" + node.toString() + "###executeJavascript")
                    .process(exchange -> {
                        Map<String, Object> arguments = (Map<String, Object>) exchange.getIn().getHeader(ScriptBuilder.ARGUMENTS);
                        Map<String, Object> output = (Map<String, Object>) arguments.get("output");
                        // TODO Output type conversion dynamically
                        exchange.getIn().setBody(output.get("value"), Integer.class);
                    })
                    .id(flow.toString() + "###" + node.toString() + "###resultJavascript")
                    .removeHeader(ScriptBuilder.ARGUMENTS)
                    .id(flow.toString() + "###" + node.toString() + "###cleanupJavascript");
            }

        }
    }
}
