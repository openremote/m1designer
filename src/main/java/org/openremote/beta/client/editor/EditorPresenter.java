package org.openremote.beta.client.editor;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.RequestFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class EditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(EditorPresenter.class);

    public EditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(RequestFailureEvent.class, action -> {
            dispatchEvent(new ShowFailureMessageEvent(
                action.getRequestFailure().getFailureMessage()
            ));
        });
    }

}
