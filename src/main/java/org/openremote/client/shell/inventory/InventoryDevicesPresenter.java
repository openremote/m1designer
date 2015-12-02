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

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsType;
import org.openremote.client.event.InventoryManagerOpenEvent;
import org.openremote.client.event.RequestFailure;
import org.openremote.client.shared.RequestPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.InventoryDevicesUpdatedEvent;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsType
public class InventoryDevicesPresenter extends RequestPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryDevicesPresenter.class);

    private static final DeviceCodec DEVICE_CODEC = GWT.create(DeviceCodec.class);

    public DeviceItem[] deviceItems = new DeviceItem[0];

    public InventoryDevicesPresenter(View view) {
        super(view);

        addListener(InventoryDevicesUpdatedEvent.class, event -> {
            loadDevices();
        });
    }

    public void openManager() {
        dispatch(new InventoryManagerOpenEvent());
    }

    @Override
    public void attached() {
        super.attached();
        loadDevices();
    }

    protected void loadDevices() {
        sendRequest(
            resource("inventory", "device").addQueryParam("onlyReady", "true").get(),
            new ListResponseCallback<Device>("Load devices", DEVICE_CODEC) {
                @Override
                protected void onResponse(List<Device> result) {
                    deviceItems = new DeviceItem[result.size()];
                    for (int i = 0; i < deviceItems.length; i++) {
                        deviceItems[i] = new DeviceItem(result.get(i));
                    }
                    notifyPath("deviceItems", deviceItems);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    deviceItems = null;
                    notifyPathNull("deviceItems");
                }
            }
        );
    }
}
