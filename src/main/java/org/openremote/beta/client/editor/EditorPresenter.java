package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.console.ConsoleMessageSendEvent;
import org.openremote.beta.client.console.ConsoleWidgetUpdatedEvent;
import org.openremote.beta.client.editor.catalog.CatalogSwitchEvent;
import org.openremote.beta.client.editor.flow.crud.FlowDeletedEvent;
import org.openremote.beta.client.editor.flow.crud.FlowSavedEvent;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.editor.flow.editor.FlowUpdatedEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestCompleteEvent;
import org.openremote.beta.client.shared.request.RequestFailureEvent;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorPresenter.class);

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(RequestCompleteEvent.class);
        addRedirectToShellView(RequestFailureEvent.class);
        addRedirectToShellView(ShowInfoEvent.class);
        addRedirectToShellView(ShowFailureEvent.class);
        addRedirectToShellView(FlowEditEvent.class);
        addRedirectToShellView(FlowUpdatedEvent.class);
        addRedirectToShellView(FlowDeletedEvent.class);
        addRedirectToShellView(MessageSendEvent.class);

        addEventListener(MessageReceivedEvent.class, event ->
            dispatchEvent("#flowEditor", event)
        );

        addEventListener(ConsoleMessageSendEvent.class, event ->
                dispatchEvent("#flowEditor", event)
        );

        addEventListener(FlowEditEvent.class, event -> {
            dispatchEvent("#editorSidebar", new CatalogSwitchEvent(true));
            dispatchEvent("#flowEditor", event);
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            dispatchEvent("#editorSidebar", new CatalogSwitchEvent(true));
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(FlowSavedEvent.class, event -> {
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event ->  {
            dispatchEvent("#flowEditor", event);
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
    }

}
