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

import static org.openremote.beta.server.route.FlowRoute.DESTINATION_SINK_ID;
import static org.openremote.beta.server.route.RouteManagementUtil.getProcessorId;
import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public class FilterRoute extends NodeRoute {

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
                    String instanceId = getInstanceId(exchange);
                    LOG.debug("Filter stores data for instance: " + instanceId);
                    instanceValues.put(instanceId, exchange.getIn().getBody());
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
                    String instanceId = getInstanceId(exchange);
                    if (instanceValues.containsKey(instanceId)) {
                        log.debug("Filter has data for instance: " + instanceId);
                        exchange.getIn().setBody(instanceValues.get(instanceId));
                        exchange.getIn().setHeader(FILTER_PASS, true);
                    } else {
                        LOG.debug("Filter received trigger but has no data for instance: " + instanceId);
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
