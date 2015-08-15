package org.openremote.beta.client.editor.flow.editor;

import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import org.openremote.beta.client.editor.flow.designer.FlowDesigner;
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
    protected LienzoPanel flowDesignerPanel;
    protected Transform flowDesignerInitialTransform;

    public FlowEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowEditEvent.class, event -> {
            this.flow = event.getFlow();
            startFlowDesigner();
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            if (flowDesigner != null && flow != null) {
                Flow ownerFlow = flow.findOwnerFlowOfSlot(event.getMessageEvent().getSinkSlotId());
                if (ownerFlow != null && ownerFlow.getId().equals(flow.getId())) {
                    flowDesigner.receiveMessageEvent(event.getMessageEvent());
                }
            }
        });
    }

    public void prepareFlowDesignerContainer(Element container) {
        this.flowDesignerPanel = new LienzoPanel();

        Window.addResizeHandler(event -> flowDesignerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight()));
        flowDesignerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight());

        // The viewport is "global", only add listeners once or you leak memory!
        flowDesignerPanel.getViewport().pushMediator(new FlowEditorViewportMediator());
        flowDesignerPanel.getViewport().addViewportTransformChangedHandler(event -> {
            if (flowDesigner != null) {
                flowDesigner.viewPortChanged();
            }
        });

        this.flowDesignerInitialTransform = flowDesignerPanel.getViewport().getTransform();

        // Needed for event propagation
        HTMLPanel containerPanel = HTMLPanel.wrap((com.google.gwt.dom.client.Element) container);
        containerPanel.add(flowDesignerPanel);
    }

    protected void startFlowDesigner() {

        flowDesignerPanel.getScene().removeAll();
        flowDesignerPanel.getViewport().setTransform(flowDesignerInitialTransform);

            flowDesigner = new FlowDesigner(flow, flowDesignerPanel.getScene()) {
            @Override
            protected void onSelectionNode(Node node) {
                dispatchEvent(new FlowDesignerNodeSelectedEvent(node));
            }
        };
        flowDesignerPanel.draw();
    }
}
