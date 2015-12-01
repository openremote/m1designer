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

package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class DeviceItem {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceItem.class);

    public Device device;
    public DeviceStatusDetail status;
    public JavaScriptObject properties;

    @JsIgnore
    public DeviceItem(Device device) {
        this.device = device;
        this.status = new DeviceStatusDetail(device.getStatus());
        properties = JavaScriptObject.createObject();
        if (device.getProperties() != null)
            properties = JsonUtils.safeEval(device.getProperties());
    }

    public Device getDevice() {
        return device;
    }

    public DeviceStatusDetail getStatus() {
        return status;
    }

    public void setStatus(DeviceStatusDetail status) {
        this.status = status;
    }
}
