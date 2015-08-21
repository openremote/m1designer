package org.openremote.beta.client.editor.flow.crud;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.InventoryRefreshEvent;
import org.openremote.beta.client.editor.flow.FlowCodec;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.shared.flow.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsExport
@JsType
public class FlowsPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowsPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public Flow[] flows;

    public FlowsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(InventoryRefreshEvent.class, event -> loadFlows());
    }

    public void loadFlows() {
        sendRequest(
            resource("flow").get(),
            new ListResponseCallback<Flow>("Load all flows", FLOW_CODEC) {
                @Override
                protected void onResponse(List<Flow> flows) {
                    FlowsPresenter.this.flows = flows.toArray(new Flow[flows.size()]);
                    notifyPath("flows", FlowsPresenter.this.flows);
                }

                @Override
                public void onFailure(RequestFailure requestFailure) {
                    super.onFailure(requestFailure);
                    FlowsPresenter.this.flows = null;
                    notifyPathNull("flows");
                }
            }
        );
    }

    protected void loadFlow(String flowId) {
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

    protected void createFlow() {
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

}
