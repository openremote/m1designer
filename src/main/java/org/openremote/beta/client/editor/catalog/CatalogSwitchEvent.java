package org.openremote.beta.client.editor.catalog;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class CatalogSwitchEvent extends Event implements NonBubblingEvent{

    protected final boolean visible;

    public CatalogSwitchEvent(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
