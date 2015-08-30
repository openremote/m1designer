package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.console.ConsoleMessageSendEvent;
import org.openremote.beta.client.console.ConsoleRefreshEvent;
import org.openremote.beta.client.console.ConsoleWidgetUpdatedEvent;
import org.openremote.beta.client.editor.flow.*;
import org.openremote.beta.client.shared.Function;
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

    public boolean flowEditorDirty = false;

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(RequestCompleteEvent.class);
        addRedirectToShellView(RequestFailureEvent.class);
        addRedirectToShellView(ShowInfoEvent.class);
        addRedirectToShellView(ShowFailureEvent.class);
        addRedirectToShellView(FlowEditEvent.class);
        addRedirectToShellView(FlowModifiedEvent.class);
        addRedirectToShellView(FlowDeletedEvent.class);
        addRedirectToShellView(ConsoleRefreshEvent.class);
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
            if (flowEditorDirty) {
                dispatchDirtyConfirmation(() -> {
                        flowEditorDirty = false;
                        notifyPath("flowEditorDirty", flowEditorDirty);
                        dispatchEvent("#editorSidebar", event);
                    }
                );
            } else {
                dispatchEvent("#editorSidebar", event);
            }
        });

        addEventListener(FlowModifiedEvent.class, event -> {
            flowEditorDirty = true;
            notifyPath("flowEditorDirty", flowEditorDirty);
        });

        addEventListener(FlowEditEvent.class, event -> {
            if (flowEditorDirty) {
                dispatchDirtyConfirmation(() -> {
                        flowEditorDirty = false;
                        notifyPath("flowEditorDirty", flowEditorDirty);
                        dispatchEvent("#editorSidebar", new FlowEditorSwitchEvent(true));
                        dispatchEvent("#flowEditor", event);
                    }
                );
            } else {
                dispatchEvent("#editorSidebar", new FlowEditorSwitchEvent(true));
                dispatchEvent("#flowEditor", event);
            }
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            flowEditorDirty = false;
            notifyPath("flowEditorDirty", flowEditorDirty);
            dispatchEvent("#editorSidebar", new FlowEditorSwitchEvent(false));
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(FlowSavedEvent.class, event -> {
            flowEditorDirty = false;
            notifyPath("flowEditorDirty", flowEditorDirty);
            dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event -> {
            dispatchEvent("#flowEditor", event);
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent("#editorSidebar", new InventoryRefreshEvent());
    }

    protected void dispatchDirtyConfirmation(Function confirmAction) {
        dispatchEvent(new ConfirmationEvent(
            "Unsaved Changes",
            "You have edited the current flow and not redeployed/saved the changes. Continue without saving changes?",
            confirmAction
        ));
    }
}
