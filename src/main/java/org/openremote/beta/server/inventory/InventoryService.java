package org.openremote.beta.server.inventory;

import org.apache.camel.StaticService;
import org.openremote.beta.server.testdata.SampleClientPresets;
import org.openremote.beta.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryService.class);


    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    public ClientPreset[] getClientPresets() {
        List<ClientPreset> list = new ArrayList<>();
        synchronized (SampleClientPresets.PRESETS) {
            Collections.addAll(list, SampleClientPresets.PRESETS);
        }
        return list.toArray(new ClientPreset[list.size()]);
    }

    public boolean putClientPreset(ClientPreset clientPreset) {
        LOG.debug("Putting client preset: " + clientPreset);
        synchronized (SampleClientPresets.PRESETS) {
            boolean updated = false;
            for (int i = 0; i < SampleClientPresets.PRESETS.length; i++) {
                ClientPreset preset = SampleClientPresets.PRESETS[i];
                if (preset.getName().equals(clientPreset.getName())) {
                    SampleClientPresets.PRESETS[i] = clientPreset;
                    updated = true;
                    break;
                }
            }
            return updated;
        }
    }

}
