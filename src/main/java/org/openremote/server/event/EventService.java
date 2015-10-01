package org.openremote.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StaticService;
import org.openremote.server.flow.FlowService;
import org.openremote.server.route.NodeRoute;
import org.openremote.server.route.RouteConstants;
import org.openremote.server.route.RouteManagementService;
import org.openremote.server.route.RouteManagementService.FlowDeploymentListener;
import org.openremote.server.route.SubflowRoute;
import org.openremote.server.route.procedure.FlowProcedureException;
import org.openremote.shared.event.*;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EventService implements StaticService, FlowDeploymentListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    public static final String INCOMING_EVENT_QUEUE = "seda://incomingEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";
    public static final String OUTGOING_EVENT_QUEUE = "seda://outgoingEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";

    final protected CamelContext context;
    final protected ProducerTemplate producerTemplate;
    final protected FlowService flowService;
    final protected RouteManagementService routeManagementService;

    public EventService(CamelContext context, FlowService flowService, RouteManagementService routeManagementService) {
        this.context = context;
        this.producerTemplate = context.createProducerTemplate();
        this.flowService = flowService;
        this.routeManagementService = routeManagementService;
        this.routeManagementService.addListener(this);
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {

    }

    public void sendEvent(Event event) {
        LOG.debug("Sending: " + event);
        producerTemplate.sendBody(OUTGOING_EVENT_QUEUE, event);
    }

    public void onEvent(FlowRequestStatusEvent flowRequestStatusEvent) {
        LOG.debug("On flow event: " + flowRequestStatusEvent);
        if (flowRequestStatusEvent.getFlowId() != null) {
            Flow flow = flowService.getFlow(flowRequestStatusEvent.getFlowId(), false);
            if (flow != null) {
                routeManagementService.notifyPhaseListeners(flow);
            } else {
                LOG.debug("Flow not found: " + flowRequestStatusEvent);
                sendEvent(
                    new FlowDeploymentFailureEvent(flowRequestStatusEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
                );
            }
        } else {
            LOG.debug("Sending status of all flows");
            Flow[] flows = flowService.getFlows();
            for (Flow flow : flows) {
                routeManagementService.notifyPhaseListeners(flow);
            }
        }
    }

    public void onEvent(FlowDeployEvent flowDeployEvent) {
        LOG.debug("On flow event: " + flowDeployEvent);
        Flow flow = flowService.getFlow(flowDeployEvent.getFlowId(), false);
        if (flow != null) {
            try {
                routeManagementService.startFlowRoutes(flow);
            } catch (FlowProcedureException ex) {
                LOG.info("Flow start failed: " + flow, ex);
                sendEvent(new FlowDeploymentFailureEvent(
                    flow,
                    ex.getPhase(),
                    ex.getClass().getCanonicalName(),
                    ex.getMessage(),
                    ex.getNode(),
                    ex.getUnprocessedNodes()
                ));
            }
        } else {
            LOG.debug("Flow not found: " + flowDeployEvent);
            sendEvent(
                new FlowDeploymentFailureEvent(flowDeployEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
            );
        }
    }

    public void onEvent(FlowStopEvent flowStopEvent) {
        LOG.debug("On flow event: " + flowStopEvent);
        Flow flow = flowService.getFlow(flowStopEvent.getFlowId(), false);
        if (flow != null) {
            try {
                routeManagementService.stopFlowRoutes(flow);
            } catch (FlowProcedureException ex) {
                LOG.info("Flow stop failed: " + flow, ex);
                sendEvent(new FlowDeploymentFailureEvent(
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
            sendEvent(
                new FlowDeploymentFailureEvent(flowStopEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
            );
        }
    }

    @Override
    public void onFlowDeployment(Flow flow, FlowDeploymentPhase phase) {
        sendEvent(
            new FlowStatusEvent(flow.getId(), phase)
        );
    }

    public void onEvent(Message message) {
        LOG.debug("### On incoming message event: " + message);
        Node node = routeManagementService.getRunningNodeOwnerOfSlot(message.getSlotId());
        if (node == null) {
            LOG.debug("No running flow/node with slot, ignoring: " + message);
            return;
        }

        if (!node.isClientAccess()) {
            LOG.debug("Client access not enabled, dropping received message event for: " + node);
            return;
        }

        LOG.debug("Processing message event with node: " + node);
        Map<String, Object> messageHeaders = new HashMap<>(
            message.hasHeaders() ? message.getHeaders() : Collections.EMPTY_MAP
        );

        messageHeaders.put(RouteConstants.SLOT_ID, message.getSlotId());

        if (message.getInstanceId() != null) {
            LOG.debug("Received instance identifier, pushing onto correlation stack: " + message.getInstanceId());
            SubflowRoute.pushOntoCorrelationStack(messageHeaders, message.getInstanceId());
        }

        String body = message.getBody();

        try {
            producerTemplate.sendBodyAndHeaders(NodeRoute.getConsumerUri(node), body, messageHeaders);
        } catch (Exception ex) {
            LOG.warn("Handling message event failed: " + message, ex);
        }
    }

    public void sendMessageEvent(Node node, Slot slot, String body, Map<String, Object> headers) {
        LOG.debug("Preparing outgoing message event for: " + node);

        if (!node.isClientAccess()) {
            LOG.debug("Client access not enabled, not sending message event to: " + node);
            return;
        }

        // We want a predictable order of headers in tests, so use a sorted map
        Map<String, Object> messageHeaders = new TreeMap<>(headers);

        String instanceId = SubflowRoute.peekCorrelationStack(messageHeaders, true, true);

        Message message = new Message(slot, instanceId, body);
        if (messageHeaders.size() > 0)
            message.setHeaders(messageHeaders);

        Map<String, Object> exchangeHeaders = new HashMap<>();
        try {
            LOG.debug("### Sending outgoing message event: " + message);
            producerTemplate.sendBodyAndHeaders(OUTGOING_EVENT_QUEUE, message, exchangeHeaders);

        } catch (Exception ex) {
            LOG.warn("Sending message event failed: " + message, ex);
        }
    }

}
