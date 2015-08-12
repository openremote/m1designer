package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.session.message.MessageSessionPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ShellPresenter extends MessageSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    public ShellPresenter(com.google.gwt.dom.client.Element view) {
        super(view);
    }

    public void sayHello() {
        dispatchEventOnView(getEditorView(), new ShowInfoEvent("Hello Editor from Console!"));

    }

}
