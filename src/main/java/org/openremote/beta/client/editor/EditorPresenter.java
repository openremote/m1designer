package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestCompleteEvent;
import org.openremote.beta.client.shared.request.RequestFailureEvent;
import org.openremote.beta.client.shared.request.RequestStartEvent;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorPresenter.class);

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventRedirect(RequestStartEvent.class, getShellView());
        addEventRedirect(RequestCompleteEvent.class, getShellView());
        addEventRedirect(RequestFailureEvent.class, getShellView());
        addEventRedirect(ShowInfoEvent.class, getShellView());
        addEventRedirect(ShowFailureEvent.class, getShellView());
        addEventRedirect(MessageSendEvent.class, getShellView());
    }

}
