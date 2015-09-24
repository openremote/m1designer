package org.openremote.beta.client.shell.nodeeditor;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.json.client.JSONObject;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.event.NodePropertiesModifiedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class NodePropertiesPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(NodePropertiesPresenter.class);

    public NodePropertiesPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void nodePropertiesChanged(String nodeId, JavaScriptObject jso) {
        String nodeProperties = new JSONObject(jso).toString();
        dispatch(new NodePropertiesModifiedEvent(nodeId, nodeProperties));
    }

}