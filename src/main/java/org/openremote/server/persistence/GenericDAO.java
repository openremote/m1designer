package org.openremote.server.persistence;

import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T, ID extends Serializable> {

    T findById(ID id);

    T findById(ID id, LockModeType lockModeType);

    T findReferenceById(ID id);

    List<T> findAll();

    Long getCount();

    T makePersistent(T entity);

    T makePersistent(T entity, boolean attemptMerge);

    void makeTransient(T entity);

    void checkVersion(T entity, boolean forceUpdate);

}