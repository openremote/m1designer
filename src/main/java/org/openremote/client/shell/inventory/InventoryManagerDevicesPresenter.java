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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;
import jsinterop.annotations.JsType;
import org.fusesource.restygwt.client.Resource;
import org.openremote.client.event.RequestFailure;
import org.openremote.client.shared.RequestPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.client.ShowFailureEvent;
import org.openremote.shared.event.client.ShowInfoEvent;
import org.openremote.shared.func.Callback;
import org.openremote.shared.inventory.Adapter;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.openremote.client.shared.Timeout.debounce;

@JsType
public class InventoryManagerDevicesPresenter extends RequestPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryManagerDevicesPresenter.class);

    private static final AdapterCodec ADAPTER_CODEC = GWT.create(AdapterCodec.class);
    private static final DeviceCodec DEVICE_CODEC = GWT.create(DeviceCodec.class);

    public Adapter[] adapters = new Adapter[0];
    public Adapter adapter;
    public JavaScriptObject adapterProperties;
    public DeviceItem[] deviceItems = new DeviceItem[0];
    public DeviceItem deviceItem;
    public JavaScriptObject deviceProperties;

    public InventoryManagerDevicesPresenter(View view) {
        super(view);
    }

    public void open() {
        reset();
        loadAdapters();
        loadDevices();
    }

    public void close() {
        reset();
    }

    public void closing(Callback switchAction) {
        confirmIfDirty(switchAction);
    }

    public void onAdapterListActivate(int selected, Callback selectAction) {
        confirmIfDirty(
            () -> {
                if (selected >= 0) {
                    setResourceLocation(resource(
                        "discovery", "adapter", adapters[selected].getId()).getUri()
                    );
                    loadAdapter();
                } else {
                    resetAdapterSelection();
                }
                selectAction.call();
            });
    }

    public void onDeviceListActivate(int selected, Callback selectAction) {
        confirmIfDirty(
            () -> {
                if (selected >= 0) {
                    deviceItem = deviceItems[selected];
                    notifyPath("deviceItem");
                } else {
                    resetDeviceSelection();
                }
                selectAction.call();
            });
    }

    public void modified() {
        setDirty(true);
        if (adapter != null && adapterProperties != null) {
            adapter.setProperties(JsonUtils.stringify(adapterProperties));
        }
    }

    public void save() {
        sendRequest(
            new Resource(getResourceLocation()).put().json(ADAPTER_CODEC.encode(adapter)),
            new StatusResponseCallback("Save adapter", 204) {
                @Override
                protected void onResponse(Response response) {
                    setDirty(false);
                    loadAdapters();
                }
            }
        );
    }

    public void refreshDevices() {
        loadDevices();
    }

    public void triggerDiscovery() {
        if (adapter == null)
            return;
        sendRequest(
            resource("discovery", "adapter", "trigger").post().json(ADAPTER_CODEC.encode(adapter)),
            new StatusResponseCallback("Trigger adapter discovery", 201) {
                @Override
                protected void onResponse(Response response) {
                    dispatch(new ShowInfoEvent("Device discovery running..."));
                    debounce("Refresh Devices", () -> loadDevices(), 1000);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    if (requestFailure.statusCode == 400 || requestFailure.statusCode == 409) {
                        dispatch(new ShowFailureEvent(requestFailure.getServerText()));
                    } else {
                        super.onFailure(requestFailure);
                    }
                }
            }
        );
    }

    protected void reset() {
        adapters = null;
        notifyPathNull("adapters");
        resetAdapterSelection();
        resetDeviceSelection();
    }

    protected void resetAdapterSelection() {
        adapter = null;
        notifyPathNull("adapter");
        adapterProperties = null;
        notifyPathNull("adapterProperties");
        setDirty(false);
        setResourceLocation(null);
    }

    protected void resetDeviceSelection() {
        deviceItem = null;
        notifyPathNull("deviceItem");
    }

    protected void loadAdapters() {
        sendRequest(
            resource("discovery", "adapter").get(),
            new ListResponseCallback<Adapter>("Load adapters", ADAPTER_CODEC) {
                @Override
                protected void onResponse(List<Adapter> result) {
                    adapters = result.toArray(new Adapter[result.size()]);
                    notifyPath("adapters", adapters);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    adapters = null;
                    notifyPathNull("adapters");
                }
            }
        );
    }

    protected void loadAdapter() {
        sendRequest(
            new Resource(resourceLocation).get(),
            new ObjectResponseCallback<Adapter>("Load adapter", ADAPTER_CODEC) {
                @Override
                protected void onResponse(Adapter result) {
                    adapter = result;
                    notifyPath("adapter");

                    adapterProperties = JavaScriptObject.createObject();
                    if (adapter.getProperties() != null)
                        adapterProperties = JsonUtils.safeEval(adapter.getProperties());
                    notifyPath("adapterProperties", adapterProperties);

                    // TODO Ugly workaround because we get on-value-changed immediately
                    debounce("AdapterLoaded", () -> {
                        setDirty(false);
                    }, 1);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    adapter = null;
                    notifyPathNull("adapter");
                }
            }
        );
    }

    protected void loadDevices() {
        sendRequest(
            resource("inventory", "device").get(),
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
