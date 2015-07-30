package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.builder.script.ScriptBuilder.javaScript;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public class FunctionNodeProcessor extends NodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionNodeProcessor.class);

    public FunctionNodeProcessor(ProducerTemplate producer, Flow flow, Node node) {
        super(producer, flow, node);
        LOG.info("### NEW FUNCTION NODE PROCESSOR: " + node);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("### FUNCTION NODE PROCESSING");
        if (node.hasProperties()) {
            String javascriptText = getString(getMap(node.getProperties()), "javascript");
            if (javascriptText != null) {
                LOG.info("### FUNCTION NODE EXECUTING JAVASCRIPT: " + javascriptText);
                Object result = javaScript(javascriptText).evaluate(exchange);
                LOG.info("### FUNCTION NODE RESULT: " + result);
                exchange.getIn().setBody(result);
            }
        }
        super.process(exchange);
    }
}

