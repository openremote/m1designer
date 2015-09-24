package org.openremote.beta.client.shell.inventory;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.event.ServerReceivedEvent;
import org.openremote.beta.client.shell.event.FlowLoadEvent;
import org.openremote.beta.client.shell.event.InventoryRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class InventoryPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryPresenter.class);

    public InventoryPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(InventoryRefreshEvent.class, event -> {
            dispatchEvent("#devices", event);
            dispatchEvent("#flows", event);
            dispatchEvent("#catalog", event);
        });

        addEventListener(ServerReceivedEvent.class, event ->
            dispatchEvent("#flows", event.getEvent())
        );

        addEventListener(FlowLoadEvent.class, event ->
                dispatchEvent("#flows", event)
        );
    }
}
