package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.shared.util.Util.*;

public class ChangeNodeProcessor extends NodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeNodeProcessor.class);

    public ChangeNodeProcessor(ProducerTemplate template, Flow flow, Node node) {
        super(template, flow, node);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("### CHANGE NODE PROCESSING");
        if (node.hasProperties()) {
            String append = getString(getMap(node.getProperties()), "append");
            if (append != null) {
                exchange.getIn().setBody(exchange.getIn().getBody(String.class) + append);
            }
            String prepend = getString(getMap(node.getProperties()), "prepend");
            if (prepend != null) {
                exchange.getIn().setBody(prepend + exchange.getIn().getBody(String.class));
            }
        }
        super.process(exchange);
    }
}

