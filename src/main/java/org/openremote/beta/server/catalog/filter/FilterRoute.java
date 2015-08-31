package org.openremote.beta.server.catalog.filter;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.predicate.SinkSlotPosition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FilterRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRoute.class);

    public static final String FILTER_PASS = "FILTER_PASS";

    final protected Map<String, Object> instanceValues = new HashMap<>();

    public FilterRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        routeDefinition
            .setHeader(FILTER_PASS, constant(true))
            .id(getProcessorId("assumeFilterPass"))
            .choice()
            .id(getProcessorId("selectInputSlot"))
            .when(new SinkSlotPosition(getNode(), 0))
                .process(exchange -> {
                    LOG.debug("Filter received data on: " + node);
                    if (isNodePropertyTrue("dropEmpty").matches(exchange) && getInput(exchange).length() == 0) {
                        LOG.debug("Dropping empty message, skipping...");
                        exchange.getIn().setHeader(FILTER_PASS, false);
                    } else {
                        synchronized (instanceValues) {
                            String instanceId = getInstanceId(exchange);
                            LOG.debug("Filter stores data for instance: " + instanceId);
                            instanceValues.put(instanceId, getInput(exchange));
                        }
                    }
                })
                .id(getProcessorId("storeInstanceValue"))
                .choice()
                    .id(getProcessorId("applyRules"))
                        .when(isNodePropertyTrue("waitForTrigger"))
                            .setHeader(FILTER_PASS, constant(false))
                            .id(getProcessorId("applyWaitForTrigger"))
                // TODO: Other filter rules
                .endChoice()
            .when(new SinkSlotPosition(getNode(), 1))
                .process(exchange -> {
                    log.debug("Filter received trigger on: " + node);

                    // The trigger value must be '1' to continue
                    // TODO: Make configurable, we should accept other trigger values
                    String triggerValue = getInput(exchange);
                    try {
                        if (!Integer.valueOf(triggerValue).equals(1)) {
                            LOG.debug("Filter trigger value was not '1', skipping: " + triggerValue);
                            exchange.getIn().setHeader(FILTER_PASS, false);
                            return;
                        }
                    } catch (Exception ex) {
                        LOG.debug("Filter trigger value was not '1', skipping: " + triggerValue);
                        exchange.getIn().setHeader(FILTER_PASS, false);
                        return;
                    }

                    synchronized (instanceValues) {
                        String instanceId = getInstanceId(exchange);
                        if (instanceValues.containsKey(instanceId)) {
                            // TODO: Should be able to optionally override with trigger value instead
                            log.debug("Filter has data for instance: " + instanceId);
                            setInput(exchange, instanceValues.get(instanceId));
                            exchange.getIn().setHeader(FILTER_PASS, true);
                        } else {
                            // TODO: Should be able to optionally forward trigger value instead
                            LOG.debug("Filter received trigger but has no data for instance: " + instanceId);
                            exchange.getIn().setHeader(FILTER_PASS, false);
                        }
                    }
                })
                .id(getProcessorId("receiveTrigger"))
            .endChoice()
            .choice()
                .id(getProcessorId("checkFilterPass"))
                .when(header(FILTER_PASS).isEqualTo(false))
                    .process(exchange -> {
                        setInput(exchange, null);
                        exchange.getIn().removeHeader(FILTER_PASS);
                    })
                    .id(getProcessorId("filterClear"))
                    .stop()
                    .id(getProcessorId("filterStop"))
            .endChoice()
            .removeHeader(FILTER_PASS)
            .id(getProcessorId("filterPass"));
    }
}
