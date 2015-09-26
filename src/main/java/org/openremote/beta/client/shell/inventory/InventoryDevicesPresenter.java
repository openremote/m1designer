package org.openremote.beta.client.shell.inventory;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.event.InventoryManagerOpenEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class InventoryDevicesPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryDevicesPresenter.class);

    public String[] devices = new String[0];

    public InventoryDevicesPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void openManager() {
        dispatch(new InventoryManagerOpenEvent());
    }

}
