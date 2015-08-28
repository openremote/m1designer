package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.editor.FlowEditorSwitchEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorSidebarPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorSidebarPresenter.class);

    public boolean catalogVisible;

    public EditorSidebarPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowEditorSwitchEvent.class, event ->  {
            catalogVisible = event.isVisible();
            notifyPath("catalogVisible", catalogVisible);
            dispatchEvent("#flows", event);
        });

        addEventListener(InventoryRefreshEvent.class, event -> {
            dispatchEvent("#flows", event);
        });
    }

}
