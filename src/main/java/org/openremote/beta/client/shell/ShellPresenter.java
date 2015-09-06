package org.openremote.beta.client.shell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.IFrameElement;
import org.openremote.beta.client.console.*;
import org.openremote.beta.client.editor.flow.*;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.session.event.*;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ShellPresenter extends EventSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public boolean editorOpen;

    public ShellPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(
            ServerReceivedEvent.class,
            event -> {
                if (isEditorViewAvailable()) {
                    dispatchEvent(getEditorView(), event);
                }
                if (isConsoleViewAvailable()) {
                    dispatchEvent(getConsoleView(), event);
                }
            }
        );

        addEventListener(MessageReceivedEvent.class, event -> {
            dispatchEvent("#messageLog", event);
            if (isEditorViewAvailable()) {
                dispatchEvent(getEditorView(), event);
            }
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), event);
            }
        });

        addEventListener(MessageSendEvent.class, event -> {
            dispatchEvent("#messageLog", event);
        });

        addEventListener(ConfirmationEvent.class, event -> {
            dispatchEvent(getRequiredElement("#confirmationDialog"), event);
        });

        addEventListener(
            ConsoleMessageSendEvent.class,
            event -> {
                dispatchEvent(new MessageSendEvent(event.getMessage()));
                if (isEditorViewAvailable()) {
                    dispatchEvent(getEditorView(), event);
                }
            }
        );

        addEventListener(
            EditorOpenEvent.class,
            event -> {
                if (isConsoleViewAvailable()) {
                    dispatchEvent(getConsoleView(), event);
                }
            }
        );

        addEventListener(
            EditorCloseEvent.class,
            event -> {
                if (isConsoleViewAvailable()) {
                    dispatchEvent(getConsoleView(), event);
                }
            });

        addEventListener(
            ConsoleReadyEvent.class, event -> {
                if (!isConsoleViewAvailable())
                    return;
                if (editorOpen) {
                    dispatchEvent(getConsoleView(), new EditorOpenEvent());
                } else {
                    dispatchEvent(getConsoleView(), new EditorCloseEvent());
                }

                ClientPresetVariant clientPresetVariant = new ClientPresetVariant(
                    Browser.getWindow().getNavigator().getUserAgent(),
                    Browser.getWindow().getScreen().getWidth(),
                    Browser.getWindow().getScreen().getHeight()
                );

                loadPresetFlow(clientPresetVariant);
            }
        );

        addEventListener(
            ConsoleSwitchEvent.class, event -> {
                if (event.isMaximized() && editorOpen) {


                    dispatchEvent("#messageLog", new MessageLogCloseEvent());
                    dispatchEvent(new EditorCloseEvent());
                    editorOpen = false;
                } else if (!event.isMaximized() && !editorOpen){
                    dispatchEvent("#messageLog", new MessageLogOpenEvent());
                    dispatchEvent(new EditorOpenEvent(event.getFlow() != null ? event.getFlow().getId() : null));
                    editorOpen = true;
                }
                notifyPath("editorOpen", editorOpen);
            });

        addEventListener(
            ConsoleWidgetUpdatedEvent.class, event -> {
                if (isEditorViewAvailable()) {
                    dispatchEvent(getEditorView(), event);
                }
            }
        );

        addEventListener(
            ConsoleRefreshEvent.class, event -> {
                if (isConsoleViewAvailable()) {
                    dispatchEvent(getConsoleView(), event);
                }
            }
        );

        addEventListener(
            ConsoleWidgetSelectEvent.class, event -> {
                if (isConsoleViewAvailable()) {
                    dispatchEvent(getConsoleView(), event);
                }
            }
        );

        addEventListener(
            ConsoleWidgetSelectedEvent.class, event -> {
                if (isEditorViewAvailable()) {
                    dispatchEvent(getEditorView(), event);
                }
            }
        );

        addEventListener(
            FlowEditEvent.class,
            event -> {
                dispatchEvent("#messageLog", event);
            }
        );

        addEventListener(
            FlowModifiedEvent.class,
            event -> {
                dispatchEvent("#messageLog", event);
            }
        );

        addEventListener(
            FlowDeletedEvent.class,
            event -> {
                dispatchEvent("#messageLog", event);
            }
        );
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent(new EventSessionConnectEvent());
    }

    protected void loadPresetFlow(ClientPresetVariant clientPresetVariant) {
        sendRequest(
            false,
            false,
            resource("flow", "preset")
                .addQueryParam("agent", clientPresetVariant.getUserAgent())
                .addQueryParam("width", Integer.toString(clientPresetVariant.getWidthPixels()))
                .addQueryParam("height", Integer.toString(clientPresetVariant.getHeightPixels()))
                .get(),
            new ObjectResponseCallback<Flow>("Load preset flow", FLOW_CODEC) {
                @Override
                protected void onResponse(Flow flow) {
                    dispatchEvent(new ConsoleRefreshEvent(flow, false));
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    if (requestFailure != null && requestFailure.statusCode == 404) {
                        LOG.debug("No preset flow found...");
                    } else {
                        dispatchEvent(
                            new ShowFailureEvent("Can't initialize panel. " + requestFailure.getFailureMessage()));
                    }
                }
            }
        );
    }

    protected boolean isEditorViewAvailable() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#editor");
        Element view = frame.getContentDocument().querySelector("or-editor");
        return view != null;
    }

    protected boolean isConsoleViewAvailable() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#console");
        Element view = frame.getContentDocument().querySelector("or-console");
        return view != null;
    }

    protected Element getEditorView() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#editor");
        Element view = frame.getContentDocument().querySelector("or-editor");
        if (view == null)
            throw new IllegalArgumentException("Missing or-editor view component in editor frame.");
        return view;
    }

    protected Element getConsoleView() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#console");
        Element view = frame.getContentDocument().querySelector("or-console");
        if (view == null)
            throw new IllegalArgumentException("Missing or-console view component in console frame.");
        return view;
    }


}
