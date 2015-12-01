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
import org.openremote.shared.inventory.Device;
import org.openremote.shared.inventory.Device_;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class DeviceDAOImpl extends GenericDAOImpl<Device, String>
    implements DeviceDAO {

    public DeviceDAOImpl(EntityManager em) {
        super(em, Device.class);
    }

    @Override
    public List<Device> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Device> c = cb.createQuery(Device.class);
        Root<Device> root = c.from(Device.class);
        c.select(root).orderBy(cb.asc(root.get(Device_.status)));
        return em.createQuery(c).getResultList();
    }

}