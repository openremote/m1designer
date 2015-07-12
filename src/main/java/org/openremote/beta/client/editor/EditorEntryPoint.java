package org.openremote.beta.client.editor;

import java.util.logging.Logger;

public class EditorEntryPoint implements com.google.gwt.core.client.EntryPoint {

    private static final Logger LOG = Logger.getLogger(EditorEntryPoint.class.getName());

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
