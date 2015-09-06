package org.openremote.beta.server.testdata;

import org.openremote.beta.shared.inventory.ClientPreset;

public class SampleClientPresets {

        /* ###################################################################################### */

    public static final ClientPreset IPAD_LANDSCAPE = new ClientPreset("iPad Landscape", "iPad", 1024, 1024, 768, 768);

    /* ###################################################################################### */

    public static final ClientPreset NEXUS_5 = new ClientPreset("Nexus 5", "Nexus 5");

    /* ###################################################################################### */

    public static final ClientPreset[] PRESETS = new ClientPreset[] {
        IPAD_LANDSCAPE,
        NEXUS_5
    };
}
