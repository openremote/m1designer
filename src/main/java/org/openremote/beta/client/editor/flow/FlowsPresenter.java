package org.openremote.beta.client.editor.flow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.InventoryRefreshEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.event.ServerSendEvent;
import org.openremote.beta.shared.event.FlowRequestStatusEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsExport
@JsType
public class FlowsPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowsPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public FlowItem[] flowItems;
    public boolean flowEditorVisible;

    public FlowsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(InventoryRefreshEvent.class, event -> loadFlows());

        addEventListener(FlowEditorSwitchEvent.class, event -> {
            flowEditorVisible = event.isVisible();
            notifyPath("flowEditorVisible", flowEditorVisible);
        });

        addEventListener(FlowLoadEvent.class, event -> loadFlow(event.getFlowId()));

        addEventListener(FlowStatusEvent.class, event -> {
            updateFlowStatus(event.getFlowId(), new FlowStatusDetail(event.getPhase()));
        });
    }

    public void loadFlows() {
        sendRequest(
            resource("flow").get(),
            new ListResponseCallback<Flow>("Load all flows", FLOW_CODEC) {
                @Override
                protected void onResponse(List<Flow> flows) {
                    List<FlowItem> items = new ArrayList<>();
                    for (Flow flow : flows) {

                        // Copy last known status if we have an existing list of flows
                        FlowStatusDetail status = null;
                        if (flowItems != null) {
                            for (FlowItem existingItem : flowItems) {
                                if (existingItem.getFlow().getId().equals(flow.getId())) {
                                    status = existingItem.getStatus();
                                    break;
                                }
                            }
                        }

                        items.add(new FlowItem(flow, status));
                    }
                    flowItems = items.toArray(new FlowItem[items.size()]);
                    notifyPath("flowItems", flowItems);

                    dispatchEvent(new ServerSendEvent(new FlowRequestStatusEvent()));
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    FlowsPresenter.this.flowItems = null;
                    notifyPathNull("flowItems");
                }
            }
        );
    }

    public void loadFlow(String flowId) {
        sendRequest(
            resource("flow", flowId).get(),
            new ObjectResponseCallback<Flow>("Load flow", FLOW_CODEC) {
                @Override
                protected void onResponse(Flow flow) {
                    dispatchEvent(new FlowEditEvent(flow, false));
                }
            }
        );
    }

    public void createFlow() {
        sendRequest(
            false, false,
            resource("flow", "template").get(),
            new ObjectResponseCallback<Flow>("Create flow", FLOW_CODEC) {
                @Override
                protected void onResponse(Flow flow) {
                    dispatchEvent(new FlowEditEvent(flow, true));
                }
            }
        );
    }

    protected void updateFlowStatus(String flowId, FlowStatusDetail flowStatusDetail) {
        for (int i = 0; i < flowItems.length; i++) {
            FlowItem flowItem = flowItems[i];
            if (flowItem.getFlow().getId().equals(flowId)) {
                flowItem.setStatus(flowStatusDetail);
                notifyPath("flowItems." + i + ".statusClass", flowItem.statusClass);
            }
        }
    }

}
