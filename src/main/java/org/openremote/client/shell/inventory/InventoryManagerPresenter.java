package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class InventoryManagerPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryManagerPresenter.class);

    public InventoryManagerPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }
}
