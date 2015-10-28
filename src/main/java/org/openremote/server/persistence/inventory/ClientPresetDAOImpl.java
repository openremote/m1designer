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