package org.openremote.beta.client.flow;

import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;
import org.openremote.beta.shared.flow.Flow;

import java.util.logging.Logger;

public class FlowEntryPoint implements com.google.gwt.core.client.EntryPoint {

    private static final Logger LOG = Logger.getLogger(FlowEntryPoint.class.getName());

    @Override
    public void onModuleLoad() {

        final LienzoPanel drawPanel = new LienzoPanel(500, 500);
        drawPanel.setBackgroundColor(Constants.BACKGROUND_COLOR);

        Window.addResizeHandler(event -> drawPanel.setPixelSize(event.getWidth(), event.getHeight()));
        drawPanel.setPixelSize(Window.getClientWidth(), Window.getClientHeight());

        drawPanel.getViewport().pushMediator(new FlowEditorViewportMediator());

        FlowCodec flowCodec = GWT.create(FlowCodec.class);
        Resource flowResource = new Resource(GWT.getHostPageBaseURL() + "flow");

        // TODO: Breaks if more than one flow
        flowResource.get().send(
            new JsonCallback() {
                @Override
                public void onSuccess(Method method, JSONValue response) {
                    JSONArray flows = response.isArray();
                    for (int i = 0; i < flows.size(); i++) {
                        Flow flow = flowCodec.decode(flows.get(i));
                        FlowEditor flowEditor = new FlowEditor(flow, drawPanel.getScene());
                    }
                }

                public void onFailure(Method method, Throwable exception) {
                    Window.alert("Error: " + exception);
                }
            });

        RootPanel.get().add(drawPanel);
    }
}
