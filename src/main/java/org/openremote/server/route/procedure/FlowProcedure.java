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

import org.apache.camel.CamelContext;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowObject;
import org.openremote.shared.flow.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class FlowProcedure {

    final protected CamelContext context;
    final protected Flow flow;

    final protected Set<FlowObject> processedFlowObjects = new HashSet<>();

    public FlowProcedure(CamelContext context, Flow flow) {
        this.context = context;
        this.flow = flow;
    }

    public CamelContext getContext() {
        return context;
    }

    public Flow getFlow() {
        return flow;
    }

    public Node[] getUnprocessedNodes() {
        List<Node> list = new ArrayList<>();
        for (Node node : flow.getNodes()) {
            if (!processedFlowObjects.contains(node)) {
                list.add(node);
            }
        }
        return list.toArray(new Node[list.size()]);
    }

    public void clearProcessed() {
        processedFlowObjects.clear();
    }

    public void addProcessed(FlowObject flowObject) {
        processedFlowObjects.add(flowObject);
    }

}
