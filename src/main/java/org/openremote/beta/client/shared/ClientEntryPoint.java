package org.openremote.beta.client.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientEntryPoint implements com.google.gwt.core.client.EntryPoint {

    private static final Logger LOG = LoggerFactory.getLogger(ClientEntryPoint.class);

    @Override
    public void onModuleLoad() {
        LOG.debug("GWT client ready...");
        onModuleReady();
    }

    private native void onModuleReady() /*-{
        if ($wnd.onGwtReadyClient) {
            $wnd.onGwtReadyClient();
        } else {
            $wnd.setTimeout(function () {
                this.@org.openremote.beta.client.shared.ClientEntryPoint::onModuleReady()();
            }.bind(this), 250);
        }
    }-*/;
}
