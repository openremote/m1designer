package org.openremote.beta.client.shell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.IFrameElement;
import org.openremote.beta.client.console.*;
import org.openremote.beta.client.shared.Callback;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.session.event.*;
import org.openremote.beta.client.shell.event.*;
import org.openremote.beta.client.shell.flowcontrol.FlowControlPresenter;
import org.openremote.beta.shared.event.FlowRuntimeFailureEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ShellPresenter extends EventSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public boolean consoleMaximized = true;
    public boolean consoleHasWidgets = false;
    public Flow flow;
    public boolean flowDirty;
    public Node node;

    public ShellPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(ConfirmationEvent.class, event -> {
            dispatchEvent("#confirmationDialog", event);
        });

        addEventListener(ServerReceivedEvent.class, event -> {
            // TODO should probably do more when a flow fails at runtime
            if (event.getEvent() instanceof FlowRuntimeFailureEvent) {
                FlowRuntimeFailureEvent runtimeFailureEvent = (FlowRuntimeFailureEvent) event.getEvent();
                dispatchEvent(new ShowFailureEvent(runtimeFailureEvent.getMessage(), 10000));
            } else {
                if (!consoleMaximized) {
                    dispatchEvent("#inventory", event);
                    dispatchEvent("#flowControl", event);
                    dispatchEvent("#flowEditor", event);
                }
            }
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#messageLog", event);
                dispatchEvent("#flowEditor", event.getMessage());
            }
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), event);
            }
        });

        addEventListener(MessageSendEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#messageLog", event);
                dispatchEvent("#flowEditor", event.getMessage());
            }
        });

        addEventListener(ConsoleMessageSendEvent.class, event -> {
            dispatchEvent(new MessageSendEvent(event.getMessage()));
        });

        addEventListener(ConsoleReadyEvent.class, event -> {
            ClientPresetVariant clientPresetVariant = new ClientPresetVariant(
                Browser.getWindow().getNavigator().getUserAgent(),
                Browser.getWindow().getScreen().getWidth(),
                Browser.getWindow().getScreen().getHeight()
            );
            loadPresetFlow(clientPresetVariant);
        });

        addEventListener(ConsoleSwitchEvent.class, event -> {
            consoleMaximized = event.isMaximized();
            notifyPath("consoleMaximized", consoleMaximized);
        });

        addEventListener(ConsoleMaximizeEvent.class, event-> {
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), event);
            }
        });

        addEventListener(ConsoleEditModeEvent.class, event-> {
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), event);
            }
        });

        addEventListener(ConsoleZoomEvent.class, event-> {
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), event);
            }
        });

        addEventListener(ConsoleRefreshEvent.class, event -> {
            if (isConsoleViewAvailable()) {
                dispatchEvent(
                    getConsoleView(),
                    // TODO Starting to look like spaghetti here
                    new ConsoleRefreshEvent(
                        event.getFlow(), event.isDirty(), node != null ? node.getId() : null)
                );
            }
        });

        addEventListener(ConsoleRefreshedEvent.class, event-> {
            if (!consoleMaximized) {
                consoleHasWidgets = event.isRenderedWidgets();
                notifyPath("consoleHasWidgets", consoleHasWidgets);
            }
        });

        addEventListener(FlowLoadEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#inventory", event);
            }
        });

        addEventListener(FlowEditEvent.class, event -> {

            // Let user confirm dropping unsaved changed
            if (flow != null && flowDirty) {
                dispatchDirtyConfirmation(() -> {
                        flowDirty = false;
                        notifyPath("flowDirty", flowDirty);
                        dispatchEvent(false, event);
                    }
                );
                return;
            }

            flow = event.getFlow();
            notifyPath("flow");

            // TODO spaghetti
            node = null;
            notifyPathNull("node");

            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
                dispatchEvent("#flowEditor", event);
                dispatchEvent("#nodeEditor", event);
                dispatchEvent("#messageLog", event);
            }
        });

        addEventListener(FlowModifiedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
                dispatchEvent("#messageLog", event);
            }
        });

        addEventListener(FlowSavedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#inventory", new InventoryRefreshEvent());
            }
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            flow = null;
            notifyPathNull("flow");

            // TODO spaghetti
            node = null;
            notifyPathNull("node");

            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
                dispatchEvent("#flowEditor", event);
                dispatchEvent("#nodeEditor", event);
                dispatchEvent("#messageLog", event);
                dispatchEvent("#inventory", new InventoryRefreshEvent());
            }
        });

        addEventListener(NodeCreateEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
            }
        });

        addEventListener(NodeDeleteEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
            }
        });

        addEventListener(NodeDuplicateEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowControl", event);
            }
        });

        addEventListener(NodeAddedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowEditor", event);
            }
        });

        addEventListener(NodeModifiedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowEditor", event);
            }
        });

        addEventListener(ConsoleWidgetSelectedEvent.class, event -> {
            if (!consoleMaximized) {
                dispatchEvent("#flowEditor", new NodeSelectEvent(event.getNodeId()));
            }
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event -> {
            if (flow != null && !consoleMaximized) {
                dispatchEvent("#flowEditor", new NodeSelectEvent(event.getNodeId()));
                dispatchEvent("#nodeEditor", new NodePropertiesUpdatedEvent(event.getNodeId(), event.getProperties()));
            }
        });

        addEventListener(NodeSelectedEvent.class, event -> {
            this.node = event.getNode();
            notifyPath("node");
            if (isConsoleViewAvailable()) {
                dispatchEvent(getConsoleView(), new ConsoleWidgetSelectEvent(event.getNode().getId()));
            }
            if (!consoleMaximized) {
                dispatchEvent("#nodeEditor", event);
            }
        });


        addEventListener(NodeDeletedEvent.class, event -> {
            this.node = null;
            notifyPathNull("node");
            if (!consoleMaximized) {
                dispatchEvent("#flowEditor", event);
                dispatchEvent("#nodeEditor", event);
            }
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatchEvent(new EventSessionConnectEvent());
    }

    protected void dispatchDirtyConfirmation(Callback confirmAction) {
        dispatchEvent(new ConfirmationEvent(
            "Unsaved Changes",
            "You have edited the current flow and not redeployed/saved the changes. Continue without saving changes?",
            confirmAction
        ));
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
                    dispatchEvent(new ConsoleRefreshEvent(flow, false, null));
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

    protected boolean isConsoleViewAvailable() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#console");
        Element view = frame.getContentDocument().querySelector("or-console");
        return view != null;
    }

    protected Element getConsoleView() {
        IFrameElement frame = (IFrameElement) getRequiredElement("#console");
        Element view = frame.getContentDocument().querySelector("or-console");
        if (view == null)
            throw new IllegalArgumentException("Missing or-console view component in console frame.");
        return view;
    }


}
