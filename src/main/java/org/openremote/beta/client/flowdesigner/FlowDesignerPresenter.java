package org.openremote.beta.client.flowdesigner;

import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.openremote.beta.client.shared.Function;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;

import java.util.logging.Logger;

@JsExport
@JsType
public class FlowDesignerPresenter {

    private static final Logger LOG = Logger.getLogger(FlowDesignerPresenter.class.getName());

    public FlowEditor flowEditor;

    public Function onSelectionNodeCallback;
    public Node selectedNode;

    public void edit(final Element parent, Flow flow) {

        Element existing = parent.getFirstChildElement();
        if (existing != null) {
            parent.removeAllChildren();
        }

        LienzoPanel designerPanel = new LienzoPanel();
        designerPanel.setBackgroundColor(Constants.BACKGROUND_COLOR);

        Window.addResizeHandler(event -> designerPanel.setPixelSize(parent.getClientWidth(), parent.getClientHeight()));
        designerPanel.setPixelSize(parent.getClientWidth(), parent.getClientHeight());
        designerPanel.getViewport().pushMediator(new FlowEditorViewportMediator());

        HTMLPanel parentPanel = HTMLPanel.wrap(parent);
        parentPanel.add(designerPanel);

        flowEditor = new FlowEditor(flow, designerPanel.getScene()) {
            @Override
            protected void onSelectionNode(Node node) {
                selectedNode = node;
                if (onSelectionNodeCallback != null)
                    onSelectionNodeCallback.call();
            }
        };

        designerPanel.draw();
   }
}
