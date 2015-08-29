package org.openremote.beta.client.editor.catalog;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.catalog.CatalogCategory;
import org.openremote.beta.shared.catalog.CatalogItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsType
public class CategorizedItem {

    public CatalogCategory category;
    public CatalogItem[] catalogItems = new CatalogItem[0];

    public CategorizedItem(CatalogCategory category) {
        this.category = category;
    }

    public void addCatalogItem(CatalogItem catalogItem) {
        List<CatalogItem> list = new ArrayList<>();
        list.addAll(Arrays.asList(catalogItems));
        list.add(catalogItem);
        catalogItems = list.toArray(new CatalogItem[list.size()]);
    }
}
