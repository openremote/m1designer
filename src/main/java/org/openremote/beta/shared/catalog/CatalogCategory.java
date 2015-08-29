package org.openremote.beta.shared.catalog;

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
