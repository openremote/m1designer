/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.shell.inventory;

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsType;
import org.openremote.client.event.*;
import org.openremote.client.shared.RequestPresenter;
import org.openremote.client.shared.View;
import org.openremote.client.shell.FlowCodec;
import org.openremote.client.shell.flowcontrol.FlowStatusDetail;
import org.openremote.shared.event.FlowLoadEvent;
import org.openremote.shared.event.FlowRequestStatusEvent;
import org.openremote.shared.event.FlowStatusEvent;
import org.openremote.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JsType
public class InventoryFlowsPresenter extends RequestPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryFlowsPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public FlowItem[] flowItems = new FlowItem[0];

    public InventoryFlowsPresenter(View view) {
        super(view);

        addListener(FlowLoadEvent.class, event -> loadFlow(event.getFlowId()));

        addListener(SessionOpenedEvent.class, event-> {
            dispatch(new ServerSendEvent(new FlowRequestStatusEvent()));
        });

        addListener(FlowStatusEvent.class, event -> {
            updateFlowStatus(event.getFlowId(), new FlowStatusDetail(event.getPhase()));
        });

        addListener(FlowSavedEvent.class, event -> loadFlows());

        addListener(FlowDeletedEvent.class, event -> loadFlows());
    }

    @Override
    public void attached() {
        super.attached();
        loadFlows();
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

                    dispatch(new ServerSendEvent(new FlowRequestStatusEvent()));
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    InventoryFlowsPresenter.this.flowItems = null;
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
                    dispatch(new FlowEditEvent(flow, false));
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
                    dispatch(new FlowEditEvent(flow, true));
                }
            }
        );
    }

    protected void updateFlowStatus(String flowId, FlowStatusDetail flowStatusDetail) {
        for (int i = 0; i < flowItems.length; i++) {
            FlowItem flowItem = flowItems[i];
            if (flowItem.getFlow().getId().equals(flowId)) {
                flowItem.setStatus(flowStatusDetail);
                notifyPath("flowItems." + i + ".status.mark", flowItem.getStatus().mark);
            }
        }
    }

}
