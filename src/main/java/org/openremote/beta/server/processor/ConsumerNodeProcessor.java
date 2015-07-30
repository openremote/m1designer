package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerNodeProcessor extends NodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerNodeProcessor.class);

    public ConsumerNodeProcessor(ProducerTemplate producerTemplate, Flow flow, Node node) {
        super(producerTemplate, flow, node);
        LOG.info("### NEW CONSUMER NODE PROCESSOR: " + node);

    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.info("### CONSUMER NODE PROCESSING");
        super.process(exchange);
    }
}
