package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.message.MessageSendEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class WidgetSinkPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetSinkPresenter.class);

    public WidgetSinkPresenter(com.google.gwt.dom.client.Element gwtView) {
        super(gwtView);
    }

    public void sendMessage(Object value) {
        MessageEvent messageEvent = new MessageEvent(
            getView().getAttribute("slot"),
            getView().getAttribute("instance").length() > 0 ? getView().getAttribute("instance") : null,
            value != null ? value.toString() : null,
            null
        );
        dispatchEvent(new MessageSendEvent(messageEvent));
    }
}
