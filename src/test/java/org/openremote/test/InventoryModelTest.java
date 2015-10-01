package org.openremote.test;

import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class InventoryModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryModelTest.class);

    @Test
    public void clientPresetVariants() throws Exception {
        ClientPresetVariant ipad = new ClientPresetVariant("iPad", 1024, 768);
        ClientPresetVariant nexus5 = new ClientPresetVariant("Nexus 5", 0, 0);

        assertTrue(new ClientPreset(1l, "test", "ipad").matches(ipad));
        assertTrue(new ClientPreset(1l, "test", "iPAD").matches(ipad));
        assertTrue(new ClientPreset(1l, "test", "ipad", 1000, 1030, 750, 770).matches(ipad));
        assertTrue(new ClientPreset(1l, "test", "ipad", 1000, 1030, 0, 770).matches(ipad));
        assertTrue(new ClientPreset(1l, "test", "ipad", 1000, 1030, 750, 0).matches(ipad));
        assertFalse(new ClientPreset(1l, "test", "ipad", 1050, 1030, 750, 0).matches(ipad));
        assertFalse(new ClientPreset(1l, "test", "ipad", 1000, 1000, 750, 0).matches(ipad));
        assertFalse(new ClientPreset(1l, "test", "ipad", 0, 0, 900, 0).matches(ipad));

        assertTrue(new ClientPreset(1l, "test", "nexus").matches(nexus5));
    }


}