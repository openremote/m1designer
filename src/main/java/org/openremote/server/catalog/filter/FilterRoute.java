package org.openremote.server.catalog.filter;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.route.predicate.SinkSlotPosition;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.builder.PredicateBuilder.not;

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
                .choice()
                    .id(getProcessorId("inputChoice"))
                    .when(and(isNodePropertyTrue("dropEmpty"), isInputEmpty()))
                        .log(DEBUG, LOG, "Dropping empty message, skipping filter")
                        .setHeader(FILTER_PASS, constant(false))
                    .when(and(isNodePropertyTrue("dropFalse"), isInputFalse()))
                        .log(DEBUG, LOG, "Dropping false message, skipping filter")
                        .setHeader(FILTER_PASS, constant(false))
                    .otherwise()
                        .process(exchange -> {
                            synchronized (instanceValues) {
                                String instanceId = getInstanceId(exchange);
                                LOG.debug("Filter stores data for instance: " + instanceId);
                                instanceValues.put(instanceId, getInput(exchange));
                            }
                        })
                        .id(getProcessorId("storeInstanceValue"))
                .endChoice()
                .choice()
                    .id(getProcessorId("applyRules"))
                        .when(isNodePropertyTrue("waitForTrigger"))
                            .setHeader(FILTER_PASS, constant(false))
                            .id(getProcessorId("applyWaitForTrigger"))
                // TODO: Other filter rules
                .endChoice()
            .when(new SinkSlotPosition(getNode(), 1))
                .choice()
                    .id(getProcessorId("triggerChoice"))
                    .when(not(isInputTrue()))
                        .log(DEBUG, LOG, "Filter trigger wasn't true, skipping filter")
                        .setHeader(FILTER_PASS, constant(false))
                    .otherwise()
                        .process(exchange -> {
                            LOG.debug("Filter received trigger on: " + node);
                            synchronized (instanceValues) {
                                String instanceId = getInstanceId(exchange);
                                if (instanceValues.containsKey(instanceId)) {
                                    // TODO: Should be able to optionally override with trigger value instead
                                    LOG.debug("Filter has data for instance: " + instanceId);
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
