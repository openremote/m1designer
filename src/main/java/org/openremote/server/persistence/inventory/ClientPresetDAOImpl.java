package org.openremote.server.persistence.inventory;

import org.openremote.server.persistence.GenericDAOImpl;
import org.openremote.shared.inventory.ClientPreset;

import javax.persistence.EntityManager;

public class ClientPresetDAOImpl extends GenericDAOImpl<ClientPreset, Long>
    implements ClientPresetDAO {

    public ClientPresetDAOImpl(EntityManager em) {
        super(em, ClientPreset.class);
    }

}