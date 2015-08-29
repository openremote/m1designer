package org.openremote.beta.client.editor.flow;

import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import org.openremote.beta.client.console.ConsoleMessageSendEvent;
import org.openremote.beta.client.console.ConsoleWidgetUpdatedEvent;
import org.openremote.beta.client.editor.flow.control.FlowControlStartEvent;
import org.openremote.beta.client.editor.flow.control.FlowControlStopEvent;
import org.openremote.beta.client.editor.flow.designer.FlowDesigner;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerNodeSelectedEvent;
import org.openremote.beta.client.editor.flow.designer.FlowEditorViewportMediator;
import org.openremote.beta.client.editor.flow.node.*;
import org.openremote.beta.client.shared.Function;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.shared.session.event.ServerReceivedEvent;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class FlowEditorPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);
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
            notifyPathNull("flow");
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

        addEventListener(NodeDuplicateEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                duplicateNode(event.getNode());
            }
        });

        addEventListener(ServerReceivedEvent.class, event -> {
            dispatchEvent("#flowControl", event.getEvent());
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            onMessage(event.getMessage());
        });

        addEventListener(MessageSendEvent.class, event -> {
            onMessage(event.getMessage());
        });

        addEventListener(ConsoleMessageSendEvent.class, event -> {
            onMessage(event.getMessage());
        });

        addEventListener(ConsoleWidgetUpdatedEvent.class, event -> {
            if (flow != null && flowDesigner != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    LOG.debug("Received console widget update, persistent state modified of: " + node);
                    node.setProperties(event.getProperties());

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
        if (flowDesigner == null || flow == null || nodeType == null)
            return;

        LOG.debug("Creating node in flow designer: " + nodeType);
        String flowId = flow.getId();
        sendRequest(
            false, true,
            resource("catalog", "node", nodeType).get(),
            new ObjectResponseCallback<Node>("Create node", NODE_CODEC) {
                @Override
                protected void onResponse(Node node) {
                    // Check if this is still the same flow designer instance as before the request
                    if (flowDesigner != null && flow != null && flow.getId().equals(flowId)) {
                        addNode(node, positionX, positionY);
                    }
                }
            }
        );
    }

    public void createSubflowNode(String flowId, double positionX, double positionY) {
        if (flowDesigner == null || flow == null)
            return;

        if (flowId != null && flow.getId().equals(flowId)) {
            return; // Can't add current flow as subflow
        }

        new CreateSubflowNodeRunnable(
            flow.getId(),
            flowId,
            positionX,
            positionY
        ).call();
    }

    protected void duplicateNode(Node node) {
        if (flowDesigner == null || flow == null)
            return;

        final String flowId = flow.getId();

        sendRequest(
            false, true,
            resource("flow", "duplicate", "node").post().json(NODE_CODEC.encode(node)),
            new ObjectResponseCallback<Node>("Duplicate node", NODE_CODEC) {
                @Override
                protected void onResponse(Node dupe) {
                    if (flow != null && flow.getId().equals(flowId)) {
                        dupe.getEditorSettings().setPositionX(dupe.getEditorSettings().getPositionX() + 20);
                        dupe.getEditorSettings().setPositionY(dupe.getEditorSettings().getPositionY() + 20);

                        flow.addNode(dupe);
                        dispatchEvent(new FlowUpdatedEvent(flow));

                        LOG.debug("Adding duplicated node shape to flow designer: " + dupe);
                        flowDesigner.addNodeShape(dupe);

                        flowDesigner.selectNodeShape(dupe);
                        dispatchEvent(new FlowDesignerNodeSelectedEvent(dupe));
                    }
                }
            }
        );

    }

    protected void addNode(Node node, double positionX, double positionY) {
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

        flowDesigner.selectNodeShape(node);
        dispatchEvent(new FlowDesignerNodeSelectedEvent(node));
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

    protected void onMessage(Message message) {
        if (flowDesigner != null && flow != null) {
            Flow ownerFlow = flow.findOwnerOfSlotInAllFlows(message.getSlotId());
            if (ownerFlow != null && ownerFlow.getId().equals(flow.getId())) {
                flowDesigner.handleMessage(message);
            }
        }
    }

    protected class CreateSubflowNodeRunnable implements Function {

        final String currentFlowId;
        final String flowId;
        final double positionX;
        final double positionY;

        public CreateSubflowNodeRunnable(String currentFlowId, String flowId, double positionX, double positionY) {
            this.currentFlowId = currentFlowId;
            this.flowId = flowId;
            this.positionX = positionX;
            this.positionY = positionY;
        }

        @Override
        public void call() {
            LOG.debug("Creating subflow in flow designer: " + flowId);
            sendRequest(
                false, true,
                resource("flow", flowId, "subflow").get(),
                new ObjectResponseCallback<Node>("Create subflow node", NODE_CODEC) {
                    @Override
                    protected void onResponse(Node node) {
                        new UpdateDependenciesRunnable(currentFlowId, node, positionX, positionY).call();
                    }
                }
            );
        }
    }

    protected class UpdateDependenciesRunnable implements Function {

        final String currentFlowId;
        final Node subflowNode;
        final double positionX;
        final double positionY;

        public UpdateDependenciesRunnable(String currentFlowId, Node subflowNode, double positionX, double positionY) {
            this.currentFlowId = currentFlowId;
            this.subflowNode = subflowNode;
            this.positionX = positionX;
            this.positionY = positionY;
        }

        @Override
        public void call() {
            // Add the subflow node to a temporary copy so we don't affect the
            // flow we are editing until we are done with resolution
            Flow flowCopy = FLOW_CODEC.decode(FLOW_CODEC.encode(flow));
            flowCopy.addNode(subflowNode);

            LOG.debug("Resolving and updating dependencies: " + flowCopy);
            sendRequest(
                false, true,
                resource("flow", "resolve").post().json(FLOW_CODEC.encode(flowCopy)),
                new ObjectResponseCallback<Flow>("Get flow dependencies", FLOW_CODEC) {
                    @Override
                    protected void onResponse(Flow resolvedFlow) {
                        // Check if this is still the same flow designer instance as before the request
                        if (flowDesigner != null && flow != null && flow.getId().equals(currentFlowId)) {
                            flow.setDependencies(resolvedFlow.getDependencies());
                            addNode(subflowNode, positionX, positionY);
                        }
                    }
                }
            );
        }
    }
}
