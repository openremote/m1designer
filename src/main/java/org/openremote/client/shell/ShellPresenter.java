package org.openremote.client.shell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import org.openremote.client.event.*;
import org.openremote.client.shared.ShowFailureEvent;
import org.openremote.client.shared.ShowInfoEvent;
import org.openremote.client.shared.request.RequestCompleteEvent;
import org.openremote.client.shared.request.RequestFailure;
import org.openremote.client.shared.session.event.EventSessionConnectEvent;
import org.openremote.client.shared.session.event.EventSessionPresenter;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ShellPresenter extends EventSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public boolean shellOpened = false;
    public boolean consoleHasWidgets = false;

    public ShellPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addListener(ShellOpenEvent.class, event -> {
            shellOpened = true;
            notifyPath("shellOpened", shellOpened);
        });

        addListener(ShellCloseEvent.class, event -> {
            shellOpened = false;
            notifyPath("shellOpened", shellOpened);
        });

        addListener(ConsoleRefreshedEvent.class, event-> {
            consoleHasWidgets = event.isRenderedWidgets();
            notifyPath("consoleHasWidgets", consoleHasWidgets);
        });

        addListener(ShowInfoEvent.class, event -> {
            getViewComponent().fire(event.getType(), event);
        });

        addListener(ShowFailureEvent.class, event -> {
            getViewComponent().fire(event.getType(), event);
        });

        addListener(RequestCompleteEvent.class, event -> {
            getViewComponent().fire(event.getType(), event);
        });

        addListener(ConfirmationEvent.class, event-> {
            getViewChildComponent("#confirmationDialog").fire(event.getType(), event);
        });

        addListener(InventoryManagerOpenEvent.class, event -> {
            getViewComponent().fire(event.getType(), event);
        });
    }

    @Override
    public void attached() {
        super.attached();
        dispatch(new EventSessionConnectEvent());

        ClientPresetVariant clientPresetVariant = new ClientPresetVariant(
            Browser.getWindow().getNavigator().getUserAgent(),
            Browser.getWindow().getScreen().getWidth(),
            Browser.getWindow().getScreen().getHeight()
        );
        loadPresetFlow(clientPresetVariant);

        // TODO Dispatch to open shell/editor by default
        //dispatch(new ShellOpenEvent(null));
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
                    dispatch(new ConsoleRefreshEvent(flow));
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    if (requestFailure != null && requestFailure.statusCode == 404) {
                        LOG.debug("No preset flow found...");
                    } else {
                        dispatch(
                            new ShowFailureEvent("Can't initialize panel. " + requestFailure.getFailureMessage()));
                    }
                }
            }
        );
    }

}
