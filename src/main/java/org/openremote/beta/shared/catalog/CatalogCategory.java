package org.openremote.beta.shared.catalog;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;

@JsType
@JsExport
public enum CatalogCategory {

    WIDGETS("Widgets"),
    WIRING("Flow Wiring"),
    PROCESSORS("Processors");

    final public String label;

    CatalogCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
