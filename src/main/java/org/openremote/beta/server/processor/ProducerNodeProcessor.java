package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerNodeProcessor extends NodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerNodeProcessor.class);

    public ProducerNodeProcessor(ProducerTemplate producer, Flow flow, Node node) {
        super(producer, flow, node);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("### PRODUCER NODE PROCESSING");
        super.process(exchange);
    }
}
