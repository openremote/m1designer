package org.openremote.beta.client.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorEntryPoint implements com.google.gwt.core.client.EntryPoint {

    private static final Logger LOG = LoggerFactory.getLogger(EditorEntryPoint.class);

    @Override
    public void onModuleLoad() {
        onModuleReady();
    }

    private native void onModuleReady() /*-{
        if ($wnd.onGwtReadyEditor) {
            $wnd.onGwtReadyEditor();
        } else {
            $wnd.setTimeout(function () {
                this.@org.openremote.beta.client.editor.EditorEntryPoint::onModuleReady()();
            }.bind(this), 250);
        }
    }-*/;
}
