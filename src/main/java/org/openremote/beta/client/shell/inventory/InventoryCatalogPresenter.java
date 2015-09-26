package org.openremote.beta.client.shell.inventory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.JsUtil;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.shared.catalog.CatalogCategory;
import org.openremote.beta.shared.catalog.CatalogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsExport
@JsType
public class InventoryCatalogPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryCatalogPresenter.class);

    private static final CatalogItemCodec CATALOG_ITEM_CODEC= GWT.create(CatalogItemCodec.class);

    public CategorizedCatalogItem[] categorizedItems;

    public InventoryCatalogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    @Override
    public void attached() {
        super.attached();
        loadCatalog();
    }

    public void loadCatalog() {

        sendRequest(
            resource("catalog").get(),
            new ListResponseCallback<CatalogItem>("Load catalog", CATALOG_ITEM_CODEC) {
                @Override
                protected void onResponse(List<CatalogItem> data) {
                    categorizedItems = createCategorizedItems(data);
                    notifyPath("categorizedItems", categorizedItems);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    categorizedItems = null;
                    notifyPathNull("categorizedItems");
                }
            }
        );
    }

    public void itemSelected(CatalogItem item) {
        dispatch(new ShowInfoEvent("TODO: Implement help display for selected: " + item));
    }

    protected CategorizedCatalogItem[] createCategorizedItems(List<CatalogItem> items) {
        List<CategorizedCatalogItem> list = new ArrayList<>();
        for (CatalogItem item : items) {
            CatalogCategory category = item.getCategory();

            CategorizedCatalogItem categorizedCatalogItem;
            int index = -1;

            for (int i = 0; i < list.size(); i++) {
                CategorizedCatalogItem ci = list.get(i);
                if (ci.category == category) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                categorizedCatalogItem = list.get(index);
            } else {
                categorizedCatalogItem = new CategorizedCatalogItem(category);
                list.add(categorizedCatalogItem);
            }
            categorizedCatalogItem.addCatalogItem(item);
        }

        Collections.sort(list, (a, b) -> JsUtil.compare(a.category.getLabel(), b.category.getLabel()));
        return list.toArray(new CategorizedCatalogItem[list.size()]);
    }
}
