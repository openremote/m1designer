package org.openremote.beta.client.editor.catalog;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.catalog.CatalogItem;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class CatalogLoadedEvent extends Event {

    protected final CatalogItem[] items;

    public CatalogLoadedEvent(CatalogItem[] items) {
        this.items = items;
    }

    public CatalogItem[] getItems() {
        return items;
    }
}
