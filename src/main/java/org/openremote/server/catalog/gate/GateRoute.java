package org.openremote.server.catalog.gate;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
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

public abstract class GateRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(GateRoute.class);

    public static final String GATE_BLOCK = "GATE_BLOCK";

    final protected Map<String, Gate> instanceValues = new HashMap<>();

    protected class Gate {

        public boolean a;
        public boolean b;

        public Gate(boolean a, boolean b) {
            this.a = a;
            this.b = b;
        }
    }

    public GateRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId("selectInputSlot"))
            .when(new SinkSlotPosition(getNode(), 0))
                .log(DEBUG, LOG, "Input a received message")
                .choice()
                .id(getProcessorId("inputA"))
                .when(isInputTrue())
                    .log(DEBUG, LOG, "Input a is true")
                    .process(exchange -> {
                        synchronized (instanceValues) {
                            getInstanceState(exchange).a = true;
                        }
                    })
                    .id(getProcessorId("inputATrue"))
                .when(isInputFalse())
                    .log(DEBUG, LOG, "Input a is false")
                    .process(exchange -> {
                        synchronized (instanceValues) {
                            getInstanceState(exchange).a = false;
                        }
                    })
                    .id(getProcessorId("inputAFalse"))
                .otherwise()
                    .log(DEBUG, LOG, "Input a is not boolean")
                    .setHeader(GATE_BLOCK, constant(true))
                    .id(getProcessorId("setGateBlockA"))
                .endChoice()
            .when(new SinkSlotPosition(getNode(), 1))
                .log(DEBUG, LOG, "Input b received message")
                .choice()
                .id(getProcessorId("inputB"))
                .when(isInputTrue())
                    .log(DEBUG, LOG, "Input b is true")
                    .process(exchange -> {
                        synchronized (instanceValues) {
                            getInstanceState(exchange).b = true;
                        }
                    })
                    .id(getProcessorId("inputBTrue"))
                .when(isInputFalse())
                    .log(DEBUG, LOG, "Input b is false")
                    .process(exchange -> {
                        synchronized (instanceValues) {
                            getInstanceState(exchange).b = false;
                        }
                    })
                    .id(getProcessorId("inputBFalse"))
                .otherwise()
                    .log(DEBUG, LOG, "Input b is not boolean")
                    .setHeader(GATE_BLOCK, constant(true))
                    .id(getProcessorId("setGateBlockB"))
                    .endChoice()
            .endChoice()
            .choice()
            .id(getProcessorId("checkGateBlock"))
            .when(header(GATE_BLOCK).isEqualTo(true))
                .removeHeader(GATE_BLOCK)
                .id(getProcessorId("removeGateBlock"))
                .stop()
                .id(getProcessorId("gateStop"))
            .endChoice();
    }

    protected Gate getInstanceState(Exchange exchange) {
        String instanceId = getInstanceId(exchange);
        LOG.debug("Accessing instance state: " + instanceId);
        if (!instanceValues.containsKey(instanceId))
            instanceValues.put(instanceId, new Gate(false, false));
        return instanceValues.get(instanceId);
    }

}
