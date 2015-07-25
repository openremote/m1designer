package org.openremote.beta.client.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Function;
import org.openremote.beta.shared.flow.Flow;

import java.util.List;

@JsExport
@JsType
public class FlowsPresenter extends AbstractPresenter {

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);

    public Flow[] flows = new Flow[0];
    public Flow selectedFlow;

    public void loadFlows(Function success) {
        resource("flow").get().send(new ListResponseCallback<Flow>(FLOW_CODEC, success) {
            @Override
            protected void onResponse(List<Flow> data) {
                flows = data.toArray(new Flow[data.size()]);
            }
        });
    }

    public void loadFlow(int index, Function success) {
        resource("flow", flows[index].getId()).get().send(
            new ObjectResponseCallback<Flow>(FLOW_CODEC, success) {
                @Override
                protected void onResponse(Flow data) {
                    selectedFlow = data;
                }
            }
        );
    }

}
