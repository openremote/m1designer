package org.openremote.beta.server.catalog.function;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.script.ScriptBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.beta.server.route.InputValue;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.predicate.InputPredicate;
import org.openremote.beta.server.util.JsonUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.apache.camel.builder.script.ScriptBuilder.javaScript;
import static org.openremote.beta.server.route.InputValue.getBooleanInput;
import static org.openremote.beta.server.util.JsonUtil.JSON;

public class FunctionRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionRoute.class);

    public FunctionRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        if (getNodeProperties().has("javascript")) {
            routeDefinition
                .process(exchange -> {
                    Map<String, Object> arguments = new HashMap<>();

                    String input = getInput(exchange);

                    LOG.debug("Setting JavaScript input arguments: '" + input + "'");
                    if (input.length() > 0) {
                        arguments.put("input", input);
                        try {
                            // TODO: Should be ObjectNode?
                            arguments.put("inputJson", JSON.readValue(input, Object.class));
                            LOG.debug("Set JavaScript 'inputJson' argument: " + arguments.get("inputJson"));
                        } catch (Exception ex) {
                            LOG.debug("Not valid JSON: '" + input + "'");
                        }

                        Boolean booleanInput = getBooleanInput(exchange);
                        if (booleanInput != null) {
                            arguments.put("inputBoolean", booleanInput);
                        }

                    } else {
                        LOG.debug("Input was an empty string, converting it to null for easier scripting");
                        arguments.put("input", null);
                    }

                    exchange.getIn().setHeader(ScriptBuilder.ARGUMENTS, arguments);
                })
                .id(getProcessorId("prepareJavascript"))
                    // TODO Exception handling!
                .transform(javaScript(getNodeProperties().get("javascript").asText()))
                .id(getProcessorId("executeJavascript"))
                .process(exchange -> {
                    // Do a toString on the JS result
                    setInput(exchange, getInput(exchange));
                })
                .id(getProcessorId("resultJavascript"))
                .removeHeader(ScriptBuilder.ARGUMENTS)
                .id(getProcessorId("cleanupJavascript"));
        }
    }
}
