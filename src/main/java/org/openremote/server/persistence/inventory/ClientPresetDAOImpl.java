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

package org.openremote.server.persistence.inventory;

import org.openremote.server.persistence.GenericDAOImpl;
import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.inventory.ClientPreset_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class ClientPresetDAOImpl extends GenericDAOImpl<ClientPreset, Long>
    implements ClientPresetDAO {

    public ClientPresetDAOImpl(EntityManager em) {
        super(em, ClientPreset.class);
    }

    @Override
    public List<ClientPreset> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientPreset> c = cb.createQuery(ClientPreset.class);
        Root<ClientPreset> root = c.from(ClientPreset.class);
        c.select(root).orderBy(cb.asc(cb.lower(root.get(ClientPreset_.name))));
        return em.createQuery(c).getResultList();
    }

}