package org.openremote.client.shell.nodeeditor;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import jsinterop.annotations.JsType;
import org.openremote.client.event.NodePropertiesModifiedEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class NodePropertiesPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(NodePropertiesPresenter.class);

    public NodePropertiesPresenter(View view) {
        super(view);
    }

    public void nodePropertiesChanged(String nodeId, JavaScriptObject jso) {
        String nodeProperties = new JSONObject(jso).toString();
        dispatch(new NodePropertiesModifiedEvent(nodeId, nodeProperties));
    }

}