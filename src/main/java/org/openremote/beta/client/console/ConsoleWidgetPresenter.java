package org.openremote.beta.client.console;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.json.client.JSONObject;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ConsoleWidgetPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetPresenter.class);

    public ConsoleWidgetPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void widgetPropertiesChanged(String nodeId, JavaScriptObject jso) {
        String widgetProperties = new JSONObject(jso).toString();
        dispatchEvent(new ConsoleWidgetUpdatedEvent(nodeId, widgetProperties));
    }

}