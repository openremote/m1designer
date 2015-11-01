package org.openremote.server.persistence.flow;

import org.openremote.server.persistence.GenericDAO;
import org.openremote.shared.flow.Flow;

public interface FlowDAO extends GenericDAO<Flow, String> {

    Flow findById(String id, boolean populateNodesAndWires);

    Flow[] findSubflowDependents(String id);
}
