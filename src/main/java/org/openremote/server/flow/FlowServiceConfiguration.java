package org.openremote.server.flow;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.route.procedure.FlowProcedureException;
import org.openremote.server.web.WebserverConfiguration.RestRouteBuilder;
import org.openremote.server.inventory.InventoryService;
import org.openremote.server.route.RouteManagementService;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class FlowServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(FlowServiceConfiguration.class);

    class FlowServiceRouteBuilder extends RestRouteBuilder {
        @Override
        public void configure() throws Exception {
            super.configure();

            rest("/flow")

                .get()
                .route().id("GET all flows")
                .bean(getContext().hasService(FlowService.class), "getFlows")
                .endRest()

                .post()
                .consumes("application/json")
                .type(Flow.class)
                .route().id("POST new flow")
                .bean(getContext().hasService(FlowService.class), "postFlow")
                .setHeader(HTTP_RESPONSE_CODE, constant(201))
                .endRest()

                .get("/template")
                .route().id("GET flow template")
                .bean(getContext().hasService(FlowService.class), "getFlowTemplate")
                .endRest()

                .get("/preset")
                .route().id("GET flow by preset")
                .bean(getContext().hasService(FlowService.class), "getPresetFlow")
                .to("direct:restStatusNotFound")
                .endRest()

                .get("/{id}/subflow")
                .route().id("GET new subflow node by ID")
                .bean(getContext().hasService(FlowService.class), "createSubflowNode")
                .to("direct:restStatusNotFound")
                .endRest()

                .get("{id}")
                .route().id("GET flow by ID")
                .bean(getContext().hasService(FlowService.class), "getFlow")
                .to("direct:restStatusNotFound")
                .endRest()

                .post("/duplicate/node")
                .consumes("application/json")
                .type(Node.class)
                .route().id("POST node to duplicate")
                .process(exchange -> {
                    Node node = exchange.getIn().getBody(Node.class);
                    if (node != null) {
                        getContext().hasService(FlowService.class).resetCopy(node);
                        exchange.getOut().setBody(node);
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 200);
                    } else {
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                    }
                })
                .endRest()

                .post("/resolve")
                .consumes("application/json")
                .type(Flow.class)
                .route().id("POST flow to resolve its dependencies")
                .process(exchange -> {
                    Flow flow = exchange.getIn().getBody(Flow.class);
                    if (flow != null) {
                        boolean hydrateSubs = exchange.getIn().getHeader("hydrateSubs", false, Boolean.class);

                        exchange.getOut().setBody(
                            getContext().hasService(FlowService.class).getResolvedFlow(flow, hydrateSubs)
                        );
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 200);
                    } else {
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                    }
                })
                .endRest()

                .delete("{id}")
                .route().id("DELETE flow by ID")
                .process(exchange -> {
                    String flowId = exchange.getIn().getHeader("id", String.class);
                    try {
                        FlowService flowService = getContext().hasService(FlowService.class);
                        flowService.deleteFlow(flowId);
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                    } catch (FlowProcedureException ex) {
                        LOG.debug("Error deleting/stopping flow '" + flowId + "'", ex);
                        exchange.getIn().setBody("Error stopping flow '" + flowId + ":" + ex.getMessage());
                        exchange.getIn().setHeader(HTTP_RESPONSE_CODE, 409);
                    }
                })
                .endRest()

                .put("/{id}")
                .consumes("application/json")
                .type(Flow.class)
                .route().id("PUT flow by ID")
                .process(exchange -> {
                    Flow flow = exchange.getIn().getBody(Flow.class);
                    boolean found = getContext().hasService(FlowService.class).putFlow(flow);
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, found ? 204 : 404);
                })
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        FlowService flowService = new FlowService(
            context,
            context.hasService(RouteManagementService.class),
            context.hasService(InventoryService.class)
        );
        context.addService(flowService);

        context.addRoutes(new FlowServiceRouteBuilder());
    }

}
