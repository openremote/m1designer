package org.openremote.beta.client.flow.crud;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.flow.FlowCodec;
import org.openremote.beta.client.shared.RequestPresenter;
import org.openremote.beta.shared.flow.Flow;

import java.util.List;

@JsExport
@JsType
public class FlowsPresenter extends RequestPresenter {

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public FlowsPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowsLoadEvent.class, event -> {
            sendRequest(
                resource("flow").get(),
                new ListResponseCallback<Flow>("Load all flows", FLOW_CODEC) {
                    @Override
                    protected void onResponse(List<Flow> data) {
                        dispatchEvent(new FlowsLoadedEvent(data.toArray(new Flow[data.size()])));
                    }
                }
            );
        });

        addEventListener(FlowLoadEvent.class, event -> {
            sendRequest(
                resource("flow", event.getFlowId()).get(),
                new ObjectResponseCallback<Flow>("Load flow", FLOW_CODEC) {
                    @Override
                    protected void onResponse(Flow data) {
                        dispatchEvent(new FlowLoadedEvent(data));
                    }
                }
            );
        });
    }

}
