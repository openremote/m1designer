package org.openremote.beta.client.editor.flow.node;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.json.client.JSONObject;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class FlowNodeEditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeEditorPresenter.class);

    public FlowNodeEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void nodePropertiesChanged(String nodeId, JavaScriptObject jso) {
        String nodeProperties = new JSONObject(jso).toString();
        dispatchEvent(new NodePropertiesUpdatedEvent(nodeId, nodeProperties));
    }

}