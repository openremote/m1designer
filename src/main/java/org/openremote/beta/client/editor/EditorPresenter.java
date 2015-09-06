package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.console.*;
import org.openremote.beta.client.editor.flow.*;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestCompleteEvent;
import org.openremote.beta.client.shared.request.RequestFailureEvent;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.shared.session.event.ServerReceivedEvent;
import org.openremote.beta.client.shared.session.event.ServerSendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorPresenter.class);

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(RequestCompleteEvent.class);
        addRedirectToShellView(RequestFailureEvent.class);
        addRedirectToShellView(ShowInfoEvent.class);
        addRedirectToShellView(ShowFailureEvent.class);
        addRedirectToShellView(ConfirmationEvent.class);
        addRedirectToShellView(FlowEditEvent.class);
        addRedirectToShellView(FlowModifiedEvent.class);
        addRedirectToShellView(FlowDeletedEvent.class);
        addRedirectToShellView(ConsoleRefreshEvent.class);
        addRedirectToShellView(ConsoleWidgetSelectEvent.class);
        addRedirectToShellView(MessageSendEvent.class);
        addRedirectToShellView(ServerSendEvent.class);

        addEventListener(ServerReceivedEvent.class, event -> {
                dispatchEvent("#flowEditor", event);
                dispatchEvent("#editorSidebar", event);
            }
        );

        addEventListener(MessageReceivedEvent.class, event ->
                dispatchEvent("#flowEditor", event)
        );

        addEventListener(MessageSendEvent.class, event ->
                dispatchEvent("#flowEditor", event)
        );

        addEventListener(ConsoleMessageSendEvent.class, event ->
                dispatchEvent("#flowEditor", event)
        );

        addEventListener(FlowLoadEvent.class, event -> {
            dispatchEvent("#editorSidebar", event);
        });

        addEventListener(FlowEditEvent.class, event -> {
            dispatchEvent("#editorSidebar", new FlowEditorSwitchEvent(true));
            dispatchEvent("#flowEditor", event);
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            dispatchEvent("#editorSidebar", new FlowEditorSwitchEvent(false));
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(FlowSavedEvent.class, event -> {
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event -> {
            dispatchEvent("#flowEditor", event);
        });

        addEventListener(ConsoleWidgetSelectedEvent.class, event -> {
            dispatchEvent("#flowEditor", event);
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent("#editorSidebar", new InventoryRefreshEvent());

        String flowId = getWindowQueryArgument("flowId");
        if (flowId != null && flowId.length() > 0) {
            dispatchEvent(new FlowLoadEvent(flowId));
        }
    }

}
