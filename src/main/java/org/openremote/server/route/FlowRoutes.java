package org.openremote.server.route;

import org.openremote.shared.flow.Flow;

import java.util.ArrayList;
import java.util.List;

public class FlowRoutes {

    protected Flow flow;
    protected List<NodeRoute> nodeRoutes = new ArrayList<>();

    public FlowRoutes(Flow flow) {
        this.flow = flow;
    }

    public Flow getFlow() {
        return flow;
    }

    public List<NodeRoute> getNodeRoutes() {
        return nodeRoutes;
    }
}
