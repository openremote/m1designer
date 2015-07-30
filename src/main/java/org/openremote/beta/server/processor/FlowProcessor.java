package org.openremote.beta.server.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FlowProcessor.class);

    public static final String DESTINATION_NODE_ID = "DESTINATION_NODE_ID";
    public static final String DESTINATION_SINK_ID = "DESTINATION_SINK_ID";

    final ProducerTemplate template;
    final Flow flow;

    public FlowProcessor(ProducerTemplate template, Flow flow) {
        this.template = template;
        this.flow = flow;
        template.setDefaultEndpointUri("direct:" + flow.getId());
    }

    @Handler
    public void handle(Exchange exchange, @Header(DESTINATION_NODE_ID) String nodeId) throws Exception {
        Node destinationNode = flow.findNode(nodeId);
        LOG.info("############ HANDLING FLOW EXCHANGE: " + exchange + " TO DESTINATION: " + destinationNode);
        if (destinationNode == null)
            throw new IllegalArgumentException("In '" + flow + "' can't find destination node: " + nodeId);

        NodeProcessor nodeProcessor;
        switch (destinationNode.getType()) {
            case "Consumer":
                nodeProcessor = new ConsumerNodeProcessor(template, flow, destinationNode);
                break;
            case "Producer":
                nodeProcessor = new ProducerNodeProcessor(template, flow, destinationNode);
                break;
            case "Function":
                nodeProcessor = new FunctionNodeProcessor(template, flow, destinationNode);
                break;
            case "Change":
                nodeProcessor = new ChangeNodeProcessor(template, flow, destinationNode);
                break;
            case "Storage":
                nodeProcessor = new StorageNodeProcessor(template, flow, destinationNode);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle type of destination node: " + destinationNode);
        }
        nodeProcessor.process(exchange);
    }
}