package org.openremote.beta.client.editor.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.shared.catalog.CatalogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsExport
@JsType
public class CatalogPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogPresenter.class);

    private static final CatalogItemCodec CATALOG_ITEM_CODEC= GWT.create(CatalogItemCodec.class);

    public CatalogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(CatalogLoadEvent.class, event -> {
            sendRequest(
                resource("catalog").get(),
                new ListResponseCallback<CatalogItem>("Load catalog", CATALOG_ITEM_CODEC) {
                    @Override
                    protected void onResponse(List<CatalogItem> data) {
                        dispatchEvent(new CatalogLoadedEvent(data.toArray(new CatalogItem[data.size()])));
                    }
                }
            );
        });
    }

}
