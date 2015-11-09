package org.openremote.client.shell.inventory;

import jsinterop.annotations.JsType;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class InventoryManagerPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryManagerPresenter.class);

    public InventoryManagerPresenter(View view) {
        super(view);
    }
}
