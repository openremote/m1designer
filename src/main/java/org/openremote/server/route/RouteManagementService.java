/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.StaticService;
import org.openremote.server.route.procedure.FlowProcedureException;
import org.openremote.server.route.procedure.FlowStartProcedure;
import org.openremote.server.route.procedure.FlowStopProcedure;
import org.openremote.shared.event.FlowDeploymentPhase;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.openremote.shared.event.FlowDeploymentPhase.*;

public class RouteManagementService implements StaticService {

    public interface FlowDeploymentListener {
        void onFlowDeployment(Flow flow, FlowDeploymentPhase phase);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RouteManagementService.class);

    final protected CamelContext context;
    final protected List<FlowDeploymentListener> phaseListeners = new CopyOnWriteArrayList<>();
    final protected Set<String> startingFlows = new HashSet<>();
    final protected Map<String, FlowRoutes> runningFlows = new HashMap<>();
    final protected Set<String> stoppingFlows = new HashSet<>();

    public RouteManagementService(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    synchronized public void startFlowRoutes(Flow flow) throws FlowProcedureException {
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

    synchronized public void stopFlowRoutes(Flow flow) throws FlowProcedureException {
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
        // Used in routing, when a message arrives with a slot identifier, we need to find the (running) node behind it
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
