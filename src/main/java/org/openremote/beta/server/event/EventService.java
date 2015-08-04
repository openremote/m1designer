package org.openremote.beta.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StaticService;
import org.openremote.beta.server.flow.FlowService;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.route.procedure.FlowProcedureException;
import org.openremote.beta.shared.event.*;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    public static final String FLOW_EVENTS_QUEUE = "seda://flowEvents?multipleConsumers=true&waitForTaskToComplete=NEVER";

    final protected CamelContext context;
    final protected ProducerTemplate producerTemplate;
    final protected FlowService flowService;
    final protected RouteManagementService routeManagementService;

    public EventService(CamelContext context, FlowService flowService, RouteManagementService routeManagementService) {
        this.context = context;
        this.producerTemplate = context.createProducerTemplate();
        this.flowService = flowService;
        this.routeManagementService = routeManagementService;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {

    }

    public void onFlowEvent(FlowStartEvent flowStartEvent) {
        LOG.debug("On flow event: " + flowStartEvent);
        Flow flow = flowService.getFlow(flowStartEvent.getFlowId());
        if (flow != null) {
            try {
                routeManagementService.startFlowRoutes(context, flow);
                producerTemplate.sendBody(FLOW_EVENTS_QUEUE, new FlowStartedEvent(flow));
            } catch (FlowProcedureException ex) {
                LOG.info("Flow start failed: " + flow, ex);
                producerTemplate.sendBody(FLOW_EVENTS_QUEUE, new FlowManagementFailure(
                    flow,
                    ex.getPhase(),
                    ex.getClass().getCanonicalName(),
                    ex.getMessage(),
                    ex.getNode(),
                    ex.getUnprocessedNodes()
                ));
            }
        } else {
            LOG.debug("Flow not found: " + flowStartEvent);
        }
    }

    public void onFlowEvent(FlowStopEvent flowStopEvent) {
        LOG.debug("On flow event: " + flowStopEvent);
        Flow flow = flowService.getFlow(flowStopEvent.getFlowId());
        if (flow != null) {
            try {
                routeManagementService.stopFlowRoutes(context, flow);
                producerTemplate.sendBody(FLOW_EVENTS_QUEUE, new FlowStoppedEvent(flow));
            } catch (FlowProcedureException ex) {
                LOG.info("Flow stop failed: " + flow, ex);
                producerTemplate.sendBody(FLOW_EVENTS_QUEUE, new FlowManagementFailure(
                    flow,
                    ex.getPhase(),
                    ex.getClass().getCanonicalName(),
                    ex.getMessage(),
                    ex.getNode(),
                    ex.getUnprocessedNodes()
                ));
            }
        } else {
            LOG.debug("Flow not found: " + flowStopEvent);
        }
    }
}
