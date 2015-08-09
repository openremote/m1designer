package org.openremote.beta.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.StaticService;
import org.openremote.beta.server.route.procedure.FlowProcedureException;
import org.openremote.beta.server.route.procedure.FlowStartProcedure;
import org.openremote.beta.server.route.procedure.FlowStopProcedure;
import org.openremote.beta.shared.event.FlowDeploymentPhase;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

public class RouteManagementService implements StaticService {

    public static interface FlowDeploymentListener {
        void onFlowDeployment(Flow flow, FlowDeploymentPhase phase);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RouteManagementService.class);

    final protected List<FlowDeploymentListener> phaseListeners = new CopyOnWriteArrayList<>();
    final protected Set<String> startingFlows = new HashSet<>();
    final protected Map<String, FlowRoutes> runningFlows = new HashMap<>();
    final protected Set<String> stoppingFlows = new HashSet<>();

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    synchronized public void startFlowRoutes(CamelContext context, Flow flow) throws FlowProcedureException {
        if (isRunning(flow))
            return;
        if (isStartingOrStopping(flow))
            return;

        String flowId = flow.getId();
        startingFlows.add(flowId);
        notifyPhaseListeners(flow);
        try {
            FlowRoutes flowRoutes = new FlowStartProcedure(context, flow).execute();
            runningFlows.put(flowId, flowRoutes);
        } finally {
            startingFlows.remove(flowId);
            LOG.debug("Flow routes started: " + flow);
            notifyPhaseListeners(flow);
        }
    }

    synchronized public void stopFlowRoutes(CamelContext context, Flow flow) throws FlowProcedureException {
        if (!isRunning(flow))
            return;
        if (isStartingOrStopping(flow))
            return;

        String flowId = flow.getId();
        stoppingFlows.add(flowId);
        notifyPhaseListeners(flow);
        try {
            FlowRoutes flowRoutes = runningFlows.get(flowId);
            if (flowRoutes != null) {
                new FlowStopProcedure(context, flow).execute(flowRoutes);
                // TODO Add force stop option, remove even if an exception is throw by previous line?
                runningFlows.remove(flowId);
            }
        } finally {
            stoppingFlows.remove(flowId);
            LOG.debug("Flow routes stopped: " + flow);
            notifyPhaseListeners(flow);
        }
    }

    synchronized public void addListener(FlowDeploymentListener listener) {
        phaseListeners.add(listener);
    }

    synchronized public void notifyPhaseListeners(Flow flow) {
        FlowDeploymentPhase phase = getPhase(flow);
        for (FlowDeploymentListener phaseListener : phaseListeners) {
            phaseListener.onFlowDeployment(flow, phase);
        }
    }

    synchronized public FlowDeploymentPhase getPhase(Flow flow) {
        if (startingFlows.contains(flow.getId()))
            return STARTING;
        if (stoppingFlows.contains(flow.getId()))
            return STOPPING;
        if (runningFlows.containsKey(flow.getId()))
            return DEPLOYED;
        return STOPPED;
    }

    synchronized public boolean isRunning(Flow flow) {
        String flowId = flow.getId();
        if (runningFlows.containsKey(flowId)) {
            LOG.debug("Flow running: " + flow);
            return true;
        }
        return false;
    }

    synchronized public Node getRunningNodeOwnerOfSlot(String slotId) {
        // Used in routing, we have a wire connected to a sink slot, and we need to find the (running) node behind it
        for (FlowRoutes flowRoutes : runningFlows.values()) {
            Node node = flowRoutes.getFlow().findOwnerNode(slotId);
            if (node != null)
                return node;
        }
        return null;
    }

    synchronized protected boolean isStartingOrStopping(Flow flow) {
        String flowId = flow.getId();
        if (startingFlows.contains(flowId)) {
            LOG.debug("Flow start procedure in progress: " + flow);
            return true;
        }
        if (stoppingFlows.contains(flowId)) {
            LOG.debug("Flow stop procedure in progress: " + flow);
            return true;
        }
        return false;
    }
}
