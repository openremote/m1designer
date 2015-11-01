package org.openremote.server.testdata;

import org.openremote.shared.inventory.ClientPreset;

public class SampleClientPresets {

        /* ###################################################################################### */

    public static final ClientPreset IPAD_LANDSCAPE = new ClientPreset(1l, "iPad Landscape", "iPad", 1024, 1024, 768, 768);

    /* ###################################################################################### */

    public static final ClientPreset NEXUS_5 = new ClientPreset(2l, "Nexus 5", "Nexus 5");

    /* ###################################################################################### */

    static {
        NEXUS_5.setInitialFlowId(SampleEnvironmentWidget.FLOW.getId());
    }
}
