package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ConsolePresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsolePresenter.class);

    public boolean haveWidgets;

    public ConsolePresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(ConsoleRefreshEvent.class, event -> {

            Node[] clientNodes = event.getFlow().findNodes(Node.TYPE_CLIENT);

            haveWidgets = clientNodes.length > 0;

            Element container = getWidgetsContainer();
            Element child = null;
            while (((child = container.getFirstElementChild()) != null)) {
                container.removeChild(child);
            }

            for (Node clientNode : clientNodes) {
                Element textLabel = container.getOwnerDocument().createElement("or-console-widget-textlabel");
                textLabel.setAttribute("value", clientNode.getLabel());
                container.appendChild(textLabel);
            }

        });
    }

    public Element getWidgetsContainer() {
        return getView().querySelector("#widgetsContainer");
    }

}
