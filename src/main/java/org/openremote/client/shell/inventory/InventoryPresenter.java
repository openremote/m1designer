package org.openremote.client.shell.inventory;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.openremote.client.event.ShortcutEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class InventoryPresenter extends AbstractPresenter<InventoryPresenter.InventoryView> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryPresenter.class);

    @JsType(isNative = true)
    public interface InventoryView extends View {
        void toggleInventory();
    }

    public InventoryPresenter(InventoryView view) {
        super(view);

        addListener(ShortcutEvent.class, event -> {
            if (event.getKey() == 87)
                getView().toggleInventory();
        });

    }
}
