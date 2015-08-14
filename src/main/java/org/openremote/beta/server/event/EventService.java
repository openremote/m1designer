package org.openremote.beta.server.event;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StaticService;
import org.openremote.beta.server.flow.FlowService;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.RouteConstants;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.route.RouteManagementService.FlowDeploymentListener;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.server.route.procedure.FlowProcedureException;
import org.openremote.beta.shared.event.*;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public class EventService implements StaticService, FlowDeploymentListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    public static final String INCOMING_FLOW_EVENT_QUEUE = "seda://incomingFlowEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";
    public static final String OUTGOING_FLOW_EVENT_QUEUE = "seda://outgoingFlowEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";
    public static final String INCOMING_MESSAGE_EVENT_QUEUE = "seda://incomingMessageEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";
    public static final String OUTGOING_MESSAGE_EVENT_QUEUE = "seda://outgoingMessageEvent?multipleConsumers=true&waitForTaskToComplete=NEVER";

    public static boolean isClientAccessEnabled(Node node) {
        // Should we accept client message events for this node's sinks and should
        // we send client message events when a message has been received?
        return (node.hasProperties() && Boolean.valueOf(getString(getMap(node.getProperties()), "clientAccess")))
            || node.isOfType(Node.TYPE_CLIENT); // TODO ugly, fix with better node type system
    }

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

    public void onFlowEvent(FlowRequestStatusEvent flowRequestStatusEvent) {
        LOG.debug("On flow event: " + flowRequestStatusEvent);
        Flow flow = flowService.getFlow(flowRequestStatusEvent.getFlowId());
        if (flow != null) {
            routeManagementService.notifyPhaseListeners(flow);
        } else {
            LOG.debug("Flow not found: " + flowRequestStatusEvent);
            producerTemplate.sendBody(
                OUTGOING_FLOW_EVENT_QUEUE,
                new FlowDeploymentFailureEvent(flowRequestStatusEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
            );
        }
    }

    public void onFlowEvent(FlowDeployEvent flowDeployEvent) {
        LOG.debug("On flow event: " + flowDeployEvent);
        Flow flow = flowService.getFlow(flowDeployEvent.getFlowId());
        if (flow != null) {
            try {
                routeManagementService.startFlowRoutes(context, flow);
            } catch (FlowProcedureException ex) {
                LOG.info("Flow start failed: " + flow, ex);
                producerTemplate.sendBody(OUTGOING_FLOW_EVENT_QUEUE, new FlowDeploymentFailureEvent(
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
            producerTemplate.sendBody(
                OUTGOING_FLOW_EVENT_QUEUE,
                new FlowDeploymentFailureEvent(flowDeployEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
            );
        }
    }

    public void onFlowEvent(FlowStopEvent flowStopEvent) {
        LOG.debug("On flow event: " + flowStopEvent);
        Flow flow = flowService.getFlow(flowStopEvent.getFlowId());
        if (flow != null) {
            try {
                routeManagementService.stopFlowRoutes(context, flow);
            } catch (FlowProcedureException ex) {
                LOG.info("Flow stop failed: " + flow, ex);
                producerTemplate.sendBody(OUTGOING_FLOW_EVENT_QUEUE, new FlowDeploymentFailureEvent(
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
            producerTemplate.sendBody(
                OUTGOING_FLOW_EVENT_QUEUE,
                new FlowDeploymentFailureEvent(flowStopEvent.getFlowId(), FlowDeploymentPhase.NOT_FOUND)
            );
        }
    }

    @Override
    public void onFlowDeployment(Flow flow, FlowDeploymentPhase phase) {
        producerTemplate.sendBody(
            OUTGOING_FLOW_EVENT_QUEUE,
            new FlowStatusEvent(flow.getId(), phase)
        );
    }

    public void onMessageEvent(MessageEvent messageEvent) {
        LOG.debug("### On incoming message event: " + messageEvent);
        Node sinkNode = routeManagementService.getRunningNodeOwnerOfSlot(messageEvent.getSinkSlotId());
        if (sinkNode == null) {
            LOG.debug("No running flow/node with sink slot, ignoring: " + messageEvent);
            return;
        }
        // TODO: Should we check flow/node identifiers against message event data?

        if (!isClientAccessEnabled(sinkNode)) {
            LOG.debug("Client access not enabled, dropping received message event for: " + sinkNode);
            return;
        }

        LOG.debug("Processing message event with node: " + sinkNode);
        Map<String, Object> exchangeHeaders = new HashMap<>(
            messageEvent.hasHeaders() ? getMap(messageEvent.getHeaders()) : Collections.EMPTY_MAP
        );


        exchangeHeaders.put(RouteConstants.SINK_SLOT_ID, messageEvent.getSinkSlotId());

        if (messageEvent.getInstanceId() != null) {
            LOG.debug("Received instance identifier, pushing onto correlation stack: " + messageEvent.getInstanceId());
            SubflowRoute.pushOntoCorrelationStack(exchangeHeaders, messageEvent.getInstanceId());
        }

        String body = messageEvent.getBody();

        try {
            producerTemplate.sendBodyAndHeaders(NodeRoute.getConsumerUri(sinkNode), body, exchangeHeaders);
        } catch (Exception ex) {
            LOG.warn("Handling message event failed: " + messageEvent, ex);
        }
    }

    public void sendMessageEvent(Node node, Slot sink, String body, Map<String, Object> headers) {
        LOG.debug("Preparing outgoing message event for: " + node);

        if (!isClientAccessEnabled(node)) {
            LOG.debug("Client access not enabled, not sending message event to: " + node);
            return;
        }

        // We want a predictable order of headers in tests, so use a sorted map
        Map<String, Object> messageHeaders = new TreeMap<>(headers);

        String instanceId = SubflowRoute.peekCorrelationStack(messageHeaders, true, true);

        MessageEvent messageEvent = new MessageEvent(sink, instanceId, body);
        if (messageHeaders.size() > 0)
            messageEvent.setHeaders(messageHeaders);

        Map<String, Object> exchangeHeaders = new HashMap<>();
        try {
            LOG.debug("### Sending outgoing message event: " + messageEvent);
            producerTemplate.sendBodyAndHeaders(OUTGOING_MESSAGE_EVENT_QUEUE, messageEvent, exchangeHeaders);

        } catch (Exception ex) {
            LOG.warn("Sending message event failed: " + messageEvent, ex);
        }
    }
}
