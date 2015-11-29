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

package org.openremote.server.inventory;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.server.persistence.PersistenceService;
import org.openremote.server.persistence.inventory.ClientPresetDAO;
import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class InventoryService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class);

    protected final CamelContext context;

    final protected List<Device> devices = new ArrayList<>();

    public InventoryService(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    public ClientPreset[] getClientPresets() {
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            ClientPresetDAO dao = ps.getDAO(em, ClientPresetDAO.class);
            List<ClientPreset> presets = dao.findAll();
            return presets.toArray(new ClientPreset[presets.size()]);
        });
    }

    public ClientPreset getClientPreset(@Header("id") Long id) {
        LOG.debug("Get client preset: " + id);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            ClientPresetDAO dao = ps.getDAO(em, ClientPresetDAO.class);
            return dao.findById(id);
        });
    }

    public Long postClientPreset(ClientPreset clientPreset) {
        LOG.debug("Posting client preset: " + clientPreset);
        if (clientPreset.getName() == null || clientPreset.getName().length() == 0) {
            throw new IllegalArgumentException("Client preset name can't be empty");
        }
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            ClientPresetDAO dao = ps.getDAO(em, ClientPresetDAO.class);

            List<ClientPreset> presets = dao.findAll();
            for (ClientPreset preset : presets) {
                if (preset.getName().equals(clientPreset.getName())) {
                    throw new IllegalStateException("Client preset name must be unique: " + clientPreset.getName());
                }
            }

            return dao.makePersistent(clientPreset).getId();
        });
   }

    public boolean putClientPreset(Long id, ClientPreset clientPreset) {
        LOG.debug("Putting client preset: " + id);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            ClientPresetDAO dao = ps.getDAO(em, ClientPresetDAO.class);
            ClientPreset existingPreset = dao.findById(id);
            if (existingPreset == null)
                return false;
            dao.makePersistent(clientPreset);
            return true;
        });
    }

    public void deleteClientPreset(@Header("id") Long id) {
        LOG.debug("Deleting client preset: " + id);
        context.hasService(PersistenceService.class).transactional((ps, em) -> {
            ClientPresetDAO dao = ps.getDAO(em, ClientPresetDAO.class);
            ClientPreset preset = dao.findById(id);
            if (preset != null)
                dao.makeTransient(preset);
            return null;
        });
    }

}
