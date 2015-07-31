package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import static org.openremote.beta.server.route.FlowRouteManager.DESTINATION_SINK_ID;
import static org.openremote.beta.server.route.RouteManagementUtil.getProcessorId;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public class FilterRoute extends NodeRouteManager {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRoute.class);

    public static final String FILTER_PASS = "FILTER_PASS";

    final protected Map<String, Object> instanceValues = new HashMap<>();

    public FilterRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configure(RouteDefinition routeDefinition) throws Exception {

        routeDefinition
            .choice()
            .id(getProcessorId(flow, node, "selectInputSlot"))
            .when(header(DESTINATION_SINK_ID).isEqualTo(getNodeSinkId(0)))
            .process(exchange -> {
                LOG.debug("Filter received data on: " + node);
                synchronized (instanceValues) {
                    Stack<String> correlationStack = exchange.getIn().getHeader(SubflowRoute.SUBFLOW_CORRELATION_STACK, Stack.class);
                    if (correlationStack != null && correlationStack.size() > 0) {
                        String correlationId = correlationStack.peek();
                        LOG.debug("Filter storing data for subflow correlation: " + correlationId);
                        instanceValues.put(correlationId, exchange.getIn().getBody());
                    } else {
                        log.debug("Filter storing data for this node: " + node);
                        instanceValues.put(node.getIdentifier().getId(), exchange.getIn().getBody());
                    }
                }
            })
            .id(getProcessorId(flow, node, "storeInstanceValue"))
            .process(exchange ->  {

                // Assume that we continue and the filter passes
                exchange.getIn().setHeader("FILTER_PASS", true);

                // If enabled, only pass the filter (with last received value) if we receive a trigger
                if (getString(getMap(node.getProperties()), "onTrigger").toLowerCase(Locale.ROOT).equals("true")) {
                    exchange.getIn().setHeader("FILTER_PASS", false);
                }

                // TODO: Other filter rules

            })
            .id(getProcessorId(flow, node, "applyFilterRules"))
            .endChoice()
            .when(header(DESTINATION_SINK_ID).isEqualTo(getNodeSinkId(1)))
            .process(exchange -> {
                log.debug("Filter received trigger on: " + node);
                synchronized (instanceValues) {

                    Stack<String> correlationStack = exchange.getIn().getHeader(SubflowRoute.SUBFLOW_CORRELATION_STACK, Stack.class);
                    if (correlationStack != null && correlationStack.size() > 0) {
                        String correlationId = correlationStack.peek();
                        LOG.debug("Filter checking data for subflow correlation: " + correlationId);
                        if (instanceValues.containsKey(correlationId)) {
                            LOG.debug("Filter received trigger and has data for correlation: " + correlationId);
                            exchange.getIn().setBody(instanceValues.get(correlationId));
                            exchange.getIn().setHeader(FILTER_PASS, true);
                        } else {
                            LOG.debug("Filter received trigger but has no data for correlation: " + correlationId);
                        }
                    } else if (instanceValues.containsKey(node.getIdentifier().getId())) {
                        log.debug("Filter received trigger and has data for this node: " + node);
                        exchange.getIn().setBody(instanceValues.get(node.getIdentifier().getId()));
                        exchange.getIn().setHeader(FILTER_PASS, true);
                    }
                }
            })
            .id(getProcessorId(flow, node, "receiveTrigger"))
            .endChoice()
            .end()
            .choice()
            .id(getProcessorId(flow, node, "checkFilterPass"))
            .when(header(FILTER_PASS).isEqualTo(false))
            .process(exchange -> {
                exchange.getIn().setBody(null);
                exchange.getIn().removeHeader(FILTER_PASS);
            })
            .id(getProcessorId(flow, node, "filterClear"))
            .stop()
            .id(getProcessorId(flow, node, "filterStop"))
            .endChoice()
            .end()
            .removeHeader(FILTER_PASS)
            .id(getProcessorId(flow, node, "filterPass"));
    }
}
