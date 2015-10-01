package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.catalog.CatalogItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsType
public class CategorizedCatalogItem {

    public CatalogCategory category;
    public CatalogItem[] catalogItems = new CatalogItem[0];

    public CategorizedCatalogItem(CatalogCategory category) {
        this.category = category;
    }

    public void addCatalogItem(CatalogItem catalogItem) {
        List<CatalogItem> list = new ArrayList<>();
        list.addAll(Arrays.asList(catalogItems));
        list.add(catalogItem);
        catalogItems = list.toArray(new CatalogItem[list.size()]);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "category=" + category +
            ", catalogItems=" + Arrays.toString(catalogItems) +
            '}';
    }
}
