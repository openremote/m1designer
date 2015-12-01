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

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.openremote.shared.inventory.Device;

@JsType
public class DeviceStatusDetail {

    public static final String MARK_INIT = "init";
    public static final String MARK_OK = "ok";
    public static final String MARK_PROGRESS = "progress";
    public static final String MARK_PROBLEM = "problem";

    public String mark;
    public String text;

    @JsIgnore
    public DeviceStatusDetail(Device.Status status) {
        switch (status) {
            case READY:
                mark = null;
                text = Device.Status.READY.name();
                break;
            case OFFLINE:
                mark = MARK_PROGRESS;
                text = Device.Status.OFFLINE.name();
                break;
            case ONLINE:
                mark = MARK_OK;
                text = Device.Status.ONLINE.name();
                break;
            case COMMUNICATION_ERROR:
                mark = MARK_PROBLEM;
                text = "COMMUNICATION ERROR";
                break;
            case MAINTENANCE:
                mark = MARK_PROGRESS;
                text = "MAINTENANCE";
                break;
            default:
                mark = MARK_INIT;
                text = "NOT INITIALIZED, AWAITING CONFIGURATION";
        }
    }

    @Override
    public String toString() {
        return "DeviceStatusDetail{" +
            "text='" + text + '\'' +
            '}';
    }
}