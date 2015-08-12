package org.openremote.beta.client.editor.flow.editor;

import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import org.openremote.beta.client.editor.flow.designer.FlowDesigner;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerNodeSelectedEvent;
import org.openremote.beta.client.editor.flow.designer.FlowEditorViewportMediator;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class FlowEditorPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    public Flow flow;
    public FlowDesigner flowDesigner;

    public FlowEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowEditEvent.class, event -> {
            this.flow = event.getFlow();
            startFlowDesigner();
        });

        addEventRedirectOnView(MessageReceivedEvent.class, getShellView(), getView());

        addEventListener(MessageReceivedEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getMessageEvent().getFlowId())) {
                flowDesigner.receiveMessageEvent(event.getMessageEvent());
            }
        });
    }

    protected void startFlowDesigner() {

        Element container = getClearContainer();

        LienzoPanel designerPanel = new LienzoPanel();
        designerPanel.setBackgroundColor(FlowDesignerConstants.BACKGROUND_COLOR);

        Window.addResizeHandler(event -> designerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight()));
        designerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight());
        designerPanel.getViewport().pushMediator(new FlowEditorViewportMediator());

        // Needed for event propagation
        HTMLPanel containerPanel = HTMLPanel.wrap((com.google.gwt.dom.client.Element) container);
        containerPanel.add(designerPanel);

        this.flowDesigner = new FlowDesigner(flow, designerPanel.getScene()) {
            @Override
            protected void onSelectionNode(Node node) {
                dispatchEvent(new FlowDesignerNodeSelectedEvent(node));
            }
        };

        designerPanel.draw();
    }

    protected Element getClearContainer() {
        Element container = getView().querySelector("#flowDesigner");
        if (container == null) {
            throw new IllegalArgumentException("View must have a child #flowDesigner element: " + getView().getLocalName());
        }
        Element existing = container.getFirstElementChild();
        if (existing != null) {
            container.removeChild(existing);
        }
        return container;
    }
}
