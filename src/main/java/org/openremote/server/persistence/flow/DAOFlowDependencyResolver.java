package org.openremote.server.persistence.flow;

import org.openremote.server.persistence.PersistenceService;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowDependencyResolver;

import javax.persistence.EntityManager;

public abstract class DAOFlowDependencyResolver extends FlowDependencyResolver {

    final protected PersistenceService ps;
    final protected EntityManager em;

    public DAOFlowDependencyResolver(PersistenceService ps, EntityManager em) {
        this.ps = ps;
        this.em = em;
    }

    @Override
    protected String generateGlobalUniqueId() {
        return IdentifierUtil.generateGlobalUniqueId();
    }

    @Override
    protected Flow findFlow(String flowId) {
        FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
        return flowDAO.findById(flowId, true);
    }

    @Override
    protected Flow[] findSubflowDependents(String flowId) {
        FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
        return flowDAO.findSubflowDependents(flowId);
    }

    @Override
    protected void storeFlow(Flow flow) {
        FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
        flowDAO.makePersistent(flow, true);
    }
}
