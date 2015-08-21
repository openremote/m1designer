package org.openremote.beta.server.flow;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.WebserverConfiguration.RestRouteBuilder;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.route.procedure.FlowProcedureException;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class FlowServiceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(FlowServiceConfiguration.class);

    class FlowServiceRouteBuilder extends RestRouteBuilder {
        @Override
        public void configure() throws Exception {

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

                .get("{id}")
                .route().id("GET flow by ID")
                .bean(getContext().hasService(FlowService.class), "getFlow")
                .to("direct:restStatusNotFound")
                .endRest()

                .delete("{id}")
                .route().id("DELETE flow by ID")
                .process(exchange -> {
                    String flowId = exchange.getIn().getHeader("id", String.class);
                    try {
                        FlowService flowService = getContext().hasService(FlowService.class);
                        flowService.deleteFlow(flowId);
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                    } catch (Exception ex) {
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
                    try {
                        boolean found = getContext().hasService(FlowService.class).putFlow(flow);
                        if (!found) {
                            exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);
                            return;
                        }
                    } catch (Exception ex) {
                        LOG.info("Error putting flow '" + flow.getId() + "'", ex);
                        exchange.getIn().setHeader(HTTP_RESPONSE_CODE, 400);
                        return;
                    }
                    exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 204);
                })
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        FlowService flowService = new FlowService(
            context.hasService(RouteManagementService.class)
        );
        context.addService(flowService);

        context.addRoutes(new FlowServiceRouteBuilder());
    }

}
