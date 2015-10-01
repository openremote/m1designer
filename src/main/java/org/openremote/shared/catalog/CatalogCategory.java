package org.openremote.shared.catalog;

import com.google.gwt.core.client.js.JsType;

@JsType
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

    @Override
    public String toString() {
        return "CatalogCategory{" +
            "label='" + label + '\'' +
            "} " + super.toString();
    }
}
