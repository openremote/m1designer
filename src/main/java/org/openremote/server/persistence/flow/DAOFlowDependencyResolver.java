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
