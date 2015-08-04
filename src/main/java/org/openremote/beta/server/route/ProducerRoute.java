package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;

public class ProducerRoute extends NodeRoute {

    public ProducerRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(RouteDefinition routeDefinition) throws Exception {
        // Nothing to do
    }

    @Override
    protected void configureDestination(RouteDefinition routeDefinition) throws Exception {
        routeDefinition
            .process(exchange -> {
                ProducerTemplate producerTemplate = getContext().createProducerTemplate();
                Slot[] sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                // This has an invisible source slot, where many (or no) consumers receive asynchronously
                for (Slot sourceSlot : sourceSlots) {
                    Exchange exchangeCopy = copyExchange(exchange, false);
                    producerTemplate.send(
                        "seda:" + sourceSlot.getIdentifier().getId() + "?multipleConsumers=true&waitForTaskToComplete=NEVER",
                        exchangeCopy
                    );
                }
            })
            .id(getProcessorId(getFlow(), getNode(), "toQueues"));
    }
}
