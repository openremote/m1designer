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
