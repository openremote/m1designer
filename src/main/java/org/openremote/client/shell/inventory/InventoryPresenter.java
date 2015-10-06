package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.client.event.ShortcutEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class InventoryPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryPresenter.class);

    @JsType
    public interface InventoryView extends Component {
        void toggleInventory();
    }

    public InventoryPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addListener(ShortcutEvent.class, event -> {
            if (event.getKey() == 87)
                ((InventoryView)getViewComponent()).toggleInventory();
        });
    }
}
