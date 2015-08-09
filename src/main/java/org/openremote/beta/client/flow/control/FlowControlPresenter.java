package org.openremote.beta.client.flow.control;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.ShowInfoMessageEvent;
import org.openremote.beta.client.flow.FlowCodec;
import org.openremote.beta.client.shared.*;
import org.openremote.beta.shared.event.*;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class FlowControlPresenter extends SessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowControlPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);
    final protected FlowIdEventCodec FLOW_EVENT_CODEC = GWT.create(FlowIdEventCodec.class);

    final protected String serviceUrl;

    public Flow flow;

    public FlowControlPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        this.serviceUrl = getWebSocketUrl("flow");

        addEventListener(FlowRequestStatusEvent.class, event -> {
            sendMessage(FLOW_EVENT_CODEC, event);
        });

        addEventListener(FlowDeployEvent.class, event -> {
            sendMessage(FLOW_EVENT_CODEC, event);
        });

        addEventListener(FlowStopEvent.class, event -> {
            sendMessage(FLOW_EVENT_CODEC, event);
        });

        addEventListener(FlowStatusEvent.class, event -> {
            if (isConsumable(event)) {
                dispatchEvent(new FlowStatusDetailEvent(event.getPhase()));
            }
        });

        addEventListener(FlowDeploymentFailureEvent.class, event -> {
            if (isConsumable(event)) {
                dispatchEvent(new FlowStatusDetailEvent(event));
            }
        });

        addEventListener(FlowControlStartEvent.class, event -> {
            if (this.flow != null) {
                // Already controlling, set the flow and reset connection
                this.flow = event.getFlow();
                dispatchEvent(new SessionCloseEvent());
            } else {
                // Not controlling, set the flow and create a new durable connection
                this.flow = event.getFlow();
                connectAndRetryOnFailure(serviceUrl, 12, 5000); // TODO one minute?
            }
        });

        addEventListener(SessionOpenedEvent.class, event -> {
            dispatchEvent(new FlowRequestStatusEvent(flow.getId()));
        });

        addEventListener(SessionClosedCleanEvent.class, event -> {
            // Try to reconnect, continue/start editing the current flow
            if (this.flow != null) {
                dispatchEvent(new SessionConnectEvent(serviceUrl));
            }
       });
    }

    @Override
    protected void onMessageReceived(String data) {
        dispatchEvent(FLOW_EVENT_CODEC.decode(data));
    }

    protected boolean isConsumable(FlowIdEvent flowIdEvent) {
        return flow != null && flow.getId().equals(flowIdEvent.getFlowId());
    }

    public void redeployFlow() {
        stopFlow();
        sendRequest(
            resource("flow", flow.getId()).put().json(FLOW_CODEC.encode(flow)),
            new NoContentResponseCallback("Save flow") {
                @Override
                protected void onResponse() {
                    dispatchEvent(new ShowInfoMessageEvent("Flow '" + flow.getLabel() + "' saved, redeploying..."));
                    startFlow();
                }
            }
        );
    }

    public void startFlow() {
        dispatchEvent(new FlowDeployEvent(flow.getId()));
    }

    public void stopFlow() {
        dispatchEvent(new FlowStopEvent(flow.getId()));
    }
}
