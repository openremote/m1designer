package org.openremote.beta.server.inventory;

import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Random;

import static org.openremote.beta.server.testdata.SampleClientPresets.PRESETS;

public class InventoryService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class);

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    public ClientPreset[] getClientPresets() {
        return PRESETS.toArray(new ClientPreset[PRESETS.size()]);
    }

    public ClientPreset getClientPreset(@Header("id") Long id) {
        LOG.debug("Get client preset: " + id);
        synchronized (PRESETS) {
            for (ClientPreset clientPreset : PRESETS) {
                if (clientPreset.getId().equals(id)) {
                    return clientPreset;
                }
            }
        }
        return null;
    }

    public Long postClientPreset(ClientPreset clientPreset) {
        LOG.debug("Posting client preset: " + clientPreset);
        if (clientPreset.getName() == null || clientPreset.getName().length() == 0) {
            throw new IllegalArgumentException("Client preset name can't be empty");
        }
        synchronized (PRESETS) {
            for (ClientPreset preset : PRESETS) {
                if (preset.getName().equals(clientPreset.getName())) {
                    throw new IllegalStateException("Client preset name must be unique: " + clientPreset.getName());
                }
            }
            clientPreset.setId((long) new Random().nextInt(Integer.MAX_VALUE));
            PRESETS.add(clientPreset);
            return clientPreset.getId();
        }
    }

    public boolean putClientPreset(Long id, ClientPreset clientPreset) {
        LOG.debug("Putting client preset: " + id);
        synchronized (PRESETS) {
            boolean updated = false;
            for (int i = 0; i < PRESETS.size(); i++) {
                ClientPreset preset = PRESETS.get(i);
                if (preset.getId().equals(id) && clientPreset.getId().equals(id)) {
                    PRESETS.set(i, clientPreset);
                    updated = true;
                    break;
                }
            }
            return updated;
        }
    }

    public void deleteClientPreset(@Header("id") Long id) {
        LOG.debug("Deleting client preset: " + id);
        synchronized (PRESETS) {
            Iterator<ClientPreset> it = PRESETS.iterator();
            while (it.hasNext()) {
                ClientPreset clientPreset = it.next();
                if (clientPreset.getId().equals(id)) {
                    it.remove();
                    break;
                }
            }
        }
    }

}
