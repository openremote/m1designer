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

package org.openremote.server.route.procedure;

import org.openremote.shared.event.FlowDeploymentPhase;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;

public class FlowProcedureException extends Exception {

    final protected FlowDeploymentPhase phase;
    final protected Flow flow;
    final protected Node node;
    final Node[] unprocessedNodes;

    public FlowProcedureException(Throwable cause, FlowDeploymentPhase phase, Flow flow, Node node, Node[] unprocessedNodes) {
        super(cause);
        this.phase = phase;
        this.flow = flow;
        this.node = node;
        this.unprocessedNodes = unprocessedNodes;
    }

    public FlowDeploymentPhase getPhase() {
        return phase;
    }

    public Flow getFlow() {
        return flow;
    }

    public Node getNode() {
        return node;
    }

    public Node[] getUnprocessedNodes() {
        return unprocessedNodes;
    }
}
