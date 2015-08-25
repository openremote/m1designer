package org.openremote.beta.client.editor.flow.editor;

import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import org.openremote.beta.client.console.ConsoleWidgetUpdatedEvent;
import org.openremote.beta.client.editor.flow.NodeCodec;
import org.openremote.beta.client.editor.flow.control.FlowControlStartEvent;
import org.openremote.beta.client.editor.flow.control.FlowControlStopEvent;
import org.openremote.beta.client.editor.flow.crud.FlowDeletedEvent;
import org.openremote.beta.client.editor.flow.designer.FlowDesigner;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerNodeSelectedEvent;
import org.openremote.beta.client.editor.flow.designer.FlowEditorViewportMediator;
import org.openremote.beta.client.editor.flow.node.*;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class FlowEditorPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    private static final NodeCodec NODE_CODEC = GWT.create(NodeCodec.class);

    public Flow flow;
    public FlowDesigner flowDesigner;

    protected boolean isFlowNodeOpen;
    protected LienzoPanel flowDesignerPanel;
    protected Transform flowDesignerInitialTransform;

    public FlowEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(FlowEditEvent.class, event -> {
            this.flow = event.getFlow();
            notifyPath("flow");

            dispatchEvent("#flowControl", new FlowControlStartEvent(flow, event.isUnsaved()));

            dispatchEvent("#flowNode", new FlowNodeCloseEvent());
            isFlowNodeOpen = false;

            startFlowDesigner();
        });

        addEventListener(FlowUpdatedEvent.class, event -> {
            dispatchEvent("#flowControl", event);
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            this.flow = null;
            this.notifyPathNull("flow");
            dispatchEvent("#flowControl", new FlowControlStopEvent());

            dispatchEvent("#flowNode", new FlowNodeCloseEvent());
            isFlowNodeOpen = false;

            stopFlowDesigner();
        });

        addEventListener(FlowDesignerNodeSelectedEvent.class, event -> {
            dispatchEvent("#flowNode", new FlowNodeEditEvent(flow, event.getNode()));
            isFlowNodeOpen = true;
        });

        addEventListener(NodeUpdatedEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                flowDesigner.updateNodeShape(event.getNode());
                dispatchEvent(new FlowUpdatedEvent(flow));
            }
        });

        addEventListener(NodeDeletedEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                Node node = event.getNode();
                LOG.debug("Removing node shape from flow designer: " + node);
                flowDesigner.deleteNodeShape(node);
                dispatchEvent(new FlowUpdatedEvent(flow));
            }
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            if (flowDesigner != null && flow != null) {
                Flow ownerFlow = flow.findOwnerFlowOfSlot(event.getMessageEvent().getSinkSlotId());
                if (ownerFlow != null && ownerFlow.getId().equals(flow.getId())) {
                    flowDesigner.receiveMessageEvent(event.getMessageEvent());
                }
            }
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event -> {
            if (flow != null && flowDesigner != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    LOG.debug("Received console widget update: " + node);
                    node.setProperties(event.getProperties());

                    LOG.debug("Updating flow designer node shape with properties: " + node.getProperties());
                    flowDesigner.updateNodeShape(node);

                    // Careful, this should not bounce back to the console!
                    dispatchEvent("#flowControl", new FlowUpdatedEvent(flow));
                    if (isFlowNodeOpen) {
                        dispatchEvent("#flowNode", new NodePropertiesRefreshEvent(node.getId()));
                    }
                }
            }
        });
    }

    @Override
    public void attached() {
        super.attached();
        initFlowDesigner();
    }

    public void createNode(String nodeType, double positionX, double positionY) {
        if (flowDesigner == null || flow == null)
            return;

        String flowId = flow.getId();

        sendRequest(
            false, false,
            resource("catalog", "node", nodeType).get(),
            new ObjectResponseCallback<Node>("Create node", NODE_CODEC) {
                @Override
                protected void onResponse(Node node) {
                    // Check if this is still the same flow designer instance as before the request
                    if (flowDesigner != null && flow != null && flow.getId().equals(flowId)) {

                        flow.addNode(node);
                        dispatchEvent(new FlowUpdatedEvent(flow));

                        // Calculate the offset with the current transform (zoom, panning)
                        // TODO If I would know maths, I could probably do this with the transform matrices
                        Transform currentTransform = flowDesignerPanel.getViewport().getAbsoluteTransform();
                        double x = (positionX - currentTransform.getTranslateX()) * currentTransform.getInverse().getScaleX();
                        double y = (positionY - currentTransform.getTranslateY()) * currentTransform.getInverse().getScaleY();
                        node.getEditorSettings().setPositionX(x);
                        node.getEditorSettings().setPositionY(y);

                        LOG.debug("Adding node shape to flow designer: " + node);
                        flowDesigner.addNodeShape(node);
                        dispatchEvent(new FlowDesignerNodeSelectedEvent(node));
                    }
                }
            }
        );
    }

    protected void initFlowDesigner() {
        Element container = getRequiredElement("#flowDesigner");
        this.flowDesignerPanel = new LienzoPanel();

        flowDesignerPanel.setSelectCursor(Style.Cursor.MOVE);

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
            protected void onSelection(Node node) {
                dispatchEvent(new FlowDesignerNodeSelectedEvent(node));
            }

            @Override
            protected void onMoved(Node node) {
                dispatchEvent(new FlowUpdatedEvent(flowDesigner.getFlow()));
            }

            @Override
            protected void onAddition(Wire wire) {
                dispatchEvent(new FlowUpdatedEvent(flowDesigner.getFlow()));
            }

            @Override
            protected void onRemoval(Wire wire) {
                dispatchEvent(new FlowUpdatedEvent(flowDesigner.getFlow()));
            }
        };
        flowDesignerPanel.draw();
    }

    protected void stopFlowDesigner() {
        flowDesignerPanel.getScene().removeAll();
        flowDesigner = null;
    }
}
