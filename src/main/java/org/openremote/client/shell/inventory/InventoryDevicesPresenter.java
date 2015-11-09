package org.openremote.client.shell.inventory;

import jsinterop.annotations.JsType;
import org.openremote.client.event.InventoryManagerOpenEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class InventoryDevicesPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryDevicesPresenter.class);

    public String[] devices = new String[0];

    public InventoryDevicesPresenter(View view) {
        super(view);
    }

    public void openManager() {
        dispatch(new InventoryManagerOpenEvent());
    }

}
