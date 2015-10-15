package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.http.client.Response;
import org.fusesource.restygwt.client.Resource;
import org.openremote.client.event.ConfirmationEvent;
import org.openremote.client.event.RequestFailure;
import org.openremote.client.shared.RequestPresenter;
import org.openremote.client.shell.FlowCodec;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.func.Callback;
import org.openremote.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsExport
@JsType
public class InventoryManagerClientPresetsPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryManagerClientPresetsPresenter.class);

    private static final ClientPresetCodec CLIENT_PRESET_CODEC = GWT.create(ClientPresetCodec.class);
    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public ClientPreset[] clientPresets = new ClientPreset[0];
    public ClientPreset clientPreset;
    public Flow[] flows = new Flow[0];

    public InventoryManagerClientPresetsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void open() {
        reset();
        loadClientPresets();
    }

    public void close() {
        reset();
    }

    public void closing(Callback switchAction) {
        confirmIfDirty(switchAction);
    }

    public void onListActivate(int selected, Callback selectAction) {
        confirmIfDirty(
            () -> {
                if (selected >= 0) {
                    setResourceLocation(resource(
                        "inventory", "preset", clientPresets[selected].getId().toString()).getUri()
                    );
                    loadClientPreset();
                } else {
                    reset();
                }
                selectAction.call();
            });
    }

    public void modified() {
        setDirty(true);
    }

    public void create() {
        clientPreset = new ClientPreset();
        notifyPath("clientPreset");
        setDirty(true);
        setResourceLocation(null);
    }

    public void delete() {
        dispatch(new ConfirmationEvent("Delete Client Preset", "Are you sure you want to delete '" + clientPreset.getName() + "'?",
            () -> {
                sendRequest(
                    new Resource(resourceLocation).delete(),
                    new RequestPresenter.StatusResponseCallback("Delete client preset", 200) {
                        @Override
                        protected void onResponse(Response response) {
                            reset();
                            loadClientPresets();
                        }
                    }
                );
            }));
    }

    public void save() {
        if (getResourceLocation() == null) {
            sendRequest(
                resource("inventory", "preset").post().json(CLIENT_PRESET_CODEC.encode(clientPreset)),
                new RequestPresenter.StatusResponseCallback("Save new client preset", 201) {
                    @Override
                    protected void onResponse(Response response) {
                        setResourceLocation(response.getHeader("Location"));
                        setDirty(false);
                        loadClientPresets();
                    }
                }
            );
        } else {
            sendRequest(
                new Resource(getResourceLocation()).put().json(CLIENT_PRESET_CODEC.encode(clientPreset)),
                new RequestPresenter.StatusResponseCallback("Save client preset", 204) {
                    @Override
                    protected void onResponse(Response response) {
                        setDirty(false);
                        loadClientPresets();
                    }
                }
            );
        }
    }

    protected void reset() {
        clientPreset = null;
        notifyPathNull("clientPreset");
        setDirty(false);
        setResourceLocation(null);
    }

    protected void loadClientPresets() {
        sendRequest(
            resource("inventory", "preset").get(),
            new RequestPresenter.ListResponseCallback<ClientPreset>("Load client presets", CLIENT_PRESET_CODEC) {
                @Override
                protected void onResponse(List<ClientPreset> result) {
                    clientPresets = result.toArray(new ClientPreset[result.size()]);
                    notifyPath("clientPresets", clientPresets);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    clientPresets = null;
                    notifyPathNull("clientPresets");
                }
            }
        );
    }

    protected void loadClientPreset() {
        sendRequest(
            new Resource(resourceLocation).get(),
            new RequestPresenter.ObjectResponseCallback<ClientPreset>("Load client preset", CLIENT_PRESET_CODEC) {
                @Override
                protected void onResponse(ClientPreset result) {
                    clientPreset = result;
                    notifyPath("clientPreset");
                    loadFlows();
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    clientPreset = null;
                    notifyPathNull("clientPreset");
                }
            }
        );
    }

    protected void loadFlows() {
        sendRequest(
            resource("flow").get(),
            new ListResponseCallback<Flow>("Load all flows", FLOW_CODEC) {
                @Override
                protected void onResponse(List<Flow> result) {
                    flows = result.toArray(new Flow[result.size()]);
                    notifyPath("flows", flows);
                }
            }
        );
    }
}
