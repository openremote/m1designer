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

package org.openremote.server.persistence;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.List;

public abstract class GenericDAOImpl<T, ID extends Serializable>
    implements GenericDAO<T, ID> {

    protected final EntityManager em;
    protected final Class<T> entityClass;

    protected GenericDAOImpl(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public T findById(ID id) {
        return findById(id, LockModeType.NONE);
    }

    @Override
    public T findById(ID id, LockModeType lockModeType) {
        return em.find(entityClass, id, lockModeType);
    }

    @Override
    public T findReferenceById(ID id) {
        return em.getReference(entityClass, id);
    }

    @Override
    public List<T> findAll() {
        CriteriaQuery<T> c =
            em.getCriteriaBuilder().createQuery(entityClass);
        c.select(c.from(entityClass));
        return em.createQuery(c).getResultList();
    }

    @Override
    public Long getCount() {
        CriteriaQuery<Long> c =
            em.getCriteriaBuilder().createQuery(Long.class);
        c.select(em.getCriteriaBuilder().count(c.from(entityClass)));
        return em.createQuery(c).getSingleResult();
    }

    @Override
    public T makePersistent(T instance) {
        return makePersistent(instance, true);
    }

    @Override
    public T makePersistent(T instance, boolean attemptMerge) {
        if (attemptMerge) {
            return em.merge(instance);
        } else {
            em.persist(instance);
            return instance;
        }
    }

    @Override
    public void makeTransient(T instance) {
        em.remove(instance);
    }

    @Override
    public void checkVersion(T entity, boolean forceUpdate) {
        em.lock(
            entity,
            forceUpdate
                ? LockModeType.OPTIMISTIC_FORCE_INCREMENT
                : LockModeType.OPTIMISTIC
        );
    }
}