package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestCompleteEvent;
import org.openremote.beta.client.shared.request.RequestFailureEvent;
import org.openremote.beta.client.shared.request.RequestStartEvent;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorPresenter.class);

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addRedirectToShellView(RequestStartEvent.class);
        addRedirectToShellView(RequestCompleteEvent.class);
        addRedirectToShellView(RequestFailureEvent.class);
        addRedirectToShellView(ShowInfoEvent.class);
        addRedirectToShellView(ShowFailureEvent.class);
        addRedirectToShellView(EditorOpenedEvent.class);
        addRedirectToShellView(FlowEditEvent.class);
        addRedirectToShellView(MessageSendEvent.class);

        addEventListener(MessageReceivedEvent.class, event -> dispatchEvent(getRequiredChildView("#flowEditor"), event));
    }

}
