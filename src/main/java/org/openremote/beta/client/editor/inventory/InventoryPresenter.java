package org.openremote.beta.client.editor.inventory;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class InventoryPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryPresenter.class);

    public boolean managerOpen = false;

    public InventoryPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void setManagerOpen(boolean open) {
        managerOpen = open;
        notifyPath("managerOpen", managerOpen);
    }

}
