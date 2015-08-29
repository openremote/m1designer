package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.Node;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.Component;
import org.openremote.beta.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.client.shared.JsUtil.host;

@JsExport
@JsType
public class ConsoleWidgetSlotPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWidgetSlotPresenter.class);

    public ConsoleWidgetSlotPresenter(com.google.gwt.dom.client.Element gwtView) {
        super(gwtView);

        addEventListener(Message.class, this::onMessage);
    }

    protected void onMessage(Message event) {
        String propertyPath = (String) getViewComponent().get("propertyPath");
        if (propertyPath == null || propertyPath.length() == 0) {
            LOG.debug("Slot without property path, don't know how to handle message: " + getView().getOuterHTML());
        }
        // Get the parent of the slot and set its widgetProperties value (use the host element, not the document fragment)
        Node parentNode = getDOM(getView()).getParentNode();
        Component parentWidget = host(parentNode);
        String value = event.getBody();
        LOG.debug("Setting widget '" + ((Element)parentWidget).getLocalName() + "' property path '" + propertyPath + "': " + value);
        parentWidget.set("widgetProperties." + propertyPath, value);
    }
}
