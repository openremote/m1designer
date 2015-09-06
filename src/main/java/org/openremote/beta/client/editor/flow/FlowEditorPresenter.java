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
import org.openremote.beta.client.console.*;
import org.openremote.beta.client.editor.flow.designer.FlowDesigner;
import org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants;
import org.openremote.beta.client.editor.flow.node.NodeSelectedEvent;
import org.openremote.beta.client.editor.flow.designer.FlowEditorViewportMediator;
import org.openremote.beta.client.editor.flow.node.*;
import org.openremote.beta.client.shared.Callback;
import org.openremote.beta.client.shared.Consumer;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestFailure;
import org.openremote.beta.client.shared.request.RequestFailureEvent;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.shared.session.event.ServerReceivedEvent;
import org.openremote.beta.client.shared.session.event.ServerSendEvent;
import org.openremote.beta.shared.event.*;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependency;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.PATCH_LABEL_FONT_SIZE;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.PATCH_PADDING;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.PATCH_TITLE_FONT_SIZE;
import static org.openremote.beta.client.shared.Timeout.debounce;

@JsExport
@JsType
public class FlowEditorPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);
    private static final NodeCodec NODE_CODEC = GWT.create(NodeCodec.class);

    public Flow flow;

    public FlowDesigner flowDesigner;

    public String flowControlTitle;
    public boolean flowDirty = false;
    public boolean flowUnsaved;
    public FlowStatusDetail flowStatusDetail;
    public FlowDependency[] flowSuperDependencies;
    public FlowDependency[] flowSubDependencies;

    protected boolean isFlowNodeOpen;
    protected LienzoPanel flowDesignerPanel;
    protected Transform flowDesignerInitialTransform;

    public FlowEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(ServerReceivedEvent.class, event -> {
            dispatchEvent(false, event.getEvent());
        });

        addEventListener(FlowEditEvent.class, event -> {

            if (flow != null && flowDirty) {
                dispatchDirtyConfirmation(() -> {
                        setFlowDirty(false);
                        dispatchEvent(false, event);
                    }
                );
                return;
            }

            flow = event.getFlow();
            notifyPath("flow");

            setFlowUnsaved(event.isUnsaved());
            setFlowDirty(flowUnsaved);

            setDependencies();

            setFlowStatusDetail(new FlowStatusDetail("REQUESTING FLOW STATUS..."));
            dispatchEvent(new ServerSendEvent(new FlowRequestStatusEvent(flow.getId())));

            dispatchEvent("#flowNode", new FlowNodeCloseEvent());
            isFlowNodeOpen = false;

            startFlowDesigner();

            sendConsoleRefresh(flow, flowUnsaved);
        });

        addEventListener(FlowStatusEvent.class, event -> {
            if (isConsumable(event)) {
                setFlowStatusDetail(new FlowStatusDetail(event.getPhase()));
            }
        });

        addEventListener(FlowDeploymentFailureEvent.class, event -> {
            if (isConsumable(event)) {
                setFlowStatusDetail(new FlowStatusDetail(event));
            }
        });

        addEventListener(FlowModifiedEvent.class, event -> {
            setFlowStatusDetail(flowStatusDetail);
            setFlowDirty(true);
            if (event.isNotifyConsole()) {
                sendConsoleRefresh(event.getFlow(), true);
            }
        });

        addEventListener(NodeSelectedEvent.class, event -> {
            dispatchEvent("#flowNode", new FlowNodeEditEvent(flow, event.getNode()));
            isFlowNodeOpen = true;
            dispatchEvent(new ConsoleWidgetSelectEvent(event.getNode().getId()));
        });

        addEventListener(NodeUpdatedEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                flowDesigner.updateNodeShape(event.getNode());
                dispatchEvent(new FlowModifiedEvent(flow, true));
            }
        });

        addEventListener(NodeDeleteEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                deleteNode(event.node);
            }
        });

        addEventListener(NodeDuplicateEvent.class, event -> {
            if (flowDesigner != null && flow != null && flow.getId().equals(event.getFlow().getId())) {
                duplicateNode(event.getNode());
            }
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

                    dispatchEvent(new FlowModifiedEvent(flow, false));

                    if (isFlowNodeOpen) {
                        dispatchEvent("#flowNode", new NodePropertiesRefreshEvent(node.getId()));
                    }
                }
            }
        });

        addEventListener(ConsoleWidgetSelectedEvent.class, event -> {
            if (flow != null && flowDesigner != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    LOG.debug("Received console select: " + node);
                    dispatchEvent(new NodeSelectedEvent(node));
                }
            }
        });
    }

    @Override
    public void attached() {
        super.attached();
        initFlowDesigner();
    }


    /* ################################################################################# */


    public void flowPropertyChanged() {
        flowDirty = true; // Assume this is dirty, now other listeners might run
        debounce("FlowPropertyChange", () -> {
            if (flowDirty) { // If it's still dirty (after other listeners executed), tell the user
                dispatchEvent(new FlowModifiedEvent(flow, false));
            }
        }, 500);
    }

    public void editFlowDependency(Flow dependency) {
        dispatchEvent(new FlowLoadEvent(dependency.getId()));
    }

    public void startFlow() {
        dispatchEvent(new ServerSendEvent(new FlowDeployEvent(flow.getId())));
    }

    public void stopFlow() {
        dispatchEvent(new ServerSendEvent(new FlowStopEvent(flow.getId())));
    }

    public void exportFlow() {
        Flow dupe = copyFlow();
        new UpdateDependencies(dupe, true, () -> {
            LOG.info(FLOW_CODEC.encode(dupe).toString());
            dispatchEvent(new ShowInfoEvent("Export complete, see your browsers console."));
        });
    }

    public void redeployFlow() {
        new SaveFlow(copyFlow(), flowUnsaved, savedFlow -> {
            dispatchEvent(new ServerSendEvent(new FlowStopEvent(savedFlow.getId())));
            dispatchEvent(new ServerSendEvent(new FlowDeployEvent(savedFlow.getId())));
        }).call();
    }

    public void saveFlow() {
        new SaveFlow(copyFlow(), flowUnsaved).call();
    }

    public void deleteFlow() {
        new DeleteFlow(copyFlow()).call();
    }

    public void createNode(String nodeType, double positionX, double positionY) {
        if (flowDesigner == null || flow == null || nodeType == null)
            return;
        new CreateNode(flow.getId(), nodeType, new AddNode(positionX, positionY, true)).call();
    }

    public void createSubflowNode(String subflowId, double positionX, double positionY) {
        if (flowDesigner == null || flow == null)
            return;

        // Can't add current flow as subflow
        if (subflowId != null && flow.getId().equals(subflowId)) {
            return;
        }

        new CreateSubflowNode(flow.getId(), subflowId, new AddNode(positionX, positionY, true)).call();
    }

    public void deleteNode(Node node) {
        new DeleteNode(node).call();
    }


    /* ################################################################################# */

    protected void closeEditor() {
        flow = null;
        notifyPathNull("flow");

        dispatchEvent("#flowNode", new FlowNodeCloseEvent());
        isFlowNodeOpen = false;

        stopFlowDesigner();

        sendConsoleRefresh(null, false);
    }

    protected Flow copyFlow() {
        return FLOW_CODEC.decode(FLOW_CODEC.encode(flow));
    }

    protected void dispatchDirtyConfirmation(Callback confirmAction) {
        dispatchEvent(new ConfirmationEvent(
            "Unsaved Changes",
            "You have edited the current flow and not redeployed/saved the changes. Continue without saving changes?",
            confirmAction
        ));
    }

    protected void duplicateNode(Node node) {
        if (flowDesigner == null || flow == null)
            return;
        new DuplicateNode(flow.getId(), node).call();
    }

    protected void setDependencies() {
        flowSuperDependencies = flow.getSuperDependencies();
        notifyPath("flowSuperDependencies", flowSuperDependencies);

        flowSubDependencies = flow.getSubDependencies();
        notifyPath("flowSubDependencies", flowSubDependencies);
    }

    protected void setFlowUnsaved(boolean unsaved) {
        flowUnsaved = unsaved;
        notifyPath("flowUnsaved", flowUnsaved);
    }

    protected void setFlowDirty(boolean dirty) {
        flowDirty = dirty;
        notifyPath("flowDirty", flowDirty);
    }

    protected void setFlowStatusDetail(FlowStatusDetail flowStatusDetail) {
        this.flowStatusDetail = flowStatusDetail;
        notifyPath("flowStatusDetail");

        if (flowUnsaved) {
            flowControlTitle = flow.getDefaultedLabel() + " (New)";
        } else {
            flowControlTitle = flow.getDefaultedLabel();
        }
        notifyPath("flowControlTitle", flowControlTitle);
    }

    protected boolean isConsumable(FlowIdEvent flowIdEvent) {
        return flow != null && flow.getId().equals(flowIdEvent.getFlowId());
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
                dispatchEvent(new NodeSelectedEvent(node));
            }

            @Override
            protected void onMoved(Node node) {
                dispatchEvent(new FlowModifiedEvent(flowDesigner.getFlow(), true));
            }

            @Override
            protected void onAddition(Wire wire) {
                dispatchEvent(new FlowModifiedEvent(flowDesigner.getFlow(), true));
            }

            @Override
            protected void onRemoval(Wire wire) {
                dispatchEvent(new FlowModifiedEvent(flowDesigner.getFlow(), true));
            }
        };
        flowDesignerPanel.draw();
    }

    protected void stopFlowDesigner() {
        flowDesignerPanel.getScene().removeAll();
        flowDesigner = null;
    }

    protected void onMessage(Message message) {
        if (flowDesigner != null && flow != null && flow.findSlot(message.getSlotId()) != null) {
            flowDesigner.handleMessage(message);
        }
    }

    protected void sendConsoleRefresh(Flow flow, boolean flowDirty) {
        if (flow == null) {
            dispatchEvent(new ConsoleRefreshEvent(null, false));
            return;
        }

        Flow dupe = copyFlow();
        new UpdateDependencies(dupe, true, () -> dispatchEvent(new ConsoleRefreshEvent(dupe, flowDirty))).call();
    }


    /* ################################################################################# */


    protected class SaveFlow implements Callback {

        final Flow flowToSave;
        final boolean flowIsUnsaved;
        final Consumer<Flow> success;

        public SaveFlow(Flow flowToSave, boolean flowIsUnsaved) {
            this(flowToSave, flowIsUnsaved, null);
        }

        public SaveFlow(Flow flowToSave, boolean flowIsUnsaved, Consumer<Flow> success) {
            this.flowToSave = flowToSave;
            this.flowIsUnsaved = flowIsUnsaved;
            this.success = success;
        }

        @Override
        public void call() {
            Callback saveCallback;
            if (flowIsUnsaved) {
                saveCallback = () -> {
                    // Don't send dependencies to the server
                    flowToSave.clearDependencies();

                    sendRequest(
                        resource("flow").post().json(FLOW_CODEC.encode(flowToSave)),
                        new StatusResponseCallback("Save new flow", 201) {
                            @Override
                            protected void onResponse() {
                                saveSuccess();
                                if (success != null)
                                    success.accept(flowToSave);
                            }
                        }
                    );
                };
            } else {
                saveCallback = () -> {
                    // Don't send dependencies to the server
                    flowToSave.clearDependencies();

                    sendRequest(
                        resource("flow", flowToSave.getId()).put().json(FLOW_CODEC.encode(flowToSave)),
                        new StatusResponseCallback("Save flow", 204) {
                            @Override
                            protected void onResponse() {
                                saveSuccess();
                                if (success != null)
                                    success.accept(flowToSave);
                            }
                        }
                    );
                };
            }

            new CheckDependencies(flowToSave, false, affectedFlows -> {
                if (affectedFlows == null) {
                    saveCallback.call();
                } else {
                    String confirmationText =
                        "Are you sure you want to save changes to flow '" +
                            flowToSave.getDefaultedLabel() +
                            "'? " + affectedFlows;
                    dispatchEvent(
                        new ConfirmationEvent("Save Breaking Changes", confirmationText, saveCallback)
                    );
                }
            }).call();
        }

        protected void saveSuccess() {
            dispatchEvent(new FlowSavedEvent(flowToSave));
            dispatchEvent(new ShowInfoEvent("Flow '" + flowToSave.getDefaultedLabel() + "' saved."));
            if (flow != null && flow.getId().equals(flowToSave.getId())) {
                setFlowUnsaved(false);
                setFlowDirty(false);
                sendConsoleRefresh(flow, false);
            }
        }
    }

    protected class DeleteFlow implements Callback {

        final Flow flowToDelete;

        public DeleteFlow(Flow flowToDelete) {
            this.flowToDelete = flowToDelete;
        }

        @Override
        public void call() {

            StringBuilder confirmationText = new StringBuilder();
            confirmationText
                .append("Are you sure you want to delete flow '")
                .append(flowToDelete.getDefaultedLabel())
                .append("'?");

            // Remove all the consumers and producers to test what dependencies will be broken
            flowToDelete.removeProducerConsumerNodes();

            new CheckDependencies(flowToDelete, true, affectedFlows -> {

                if (affectedFlows != null)
                    confirmationText.append(" ").append(affectedFlows);

                dispatchEvent(new ConfirmationEvent(
                    "Remove Flow",
                    confirmationText.toString(),
                    new Callback() {
                        @Override
                        public void call() {
                            sendRequest(false, false,
                                resource("flow", flowToDelete.getId()).delete(),
                                new StatusResponseCallback("Delete flow", 204) {
                                    @Override
                                    protected void onResponse() {
                                        deleteSuccess();
                                    }

                                    @Override
                                    public void onFailure(RequestFailure requestFailure) {
                                        super.onFailure(requestFailure);
                                        deleteFailure(requestFailure);
                                    }
                                }
                            );
                        }
                    }
                ));
            }).call();
        }

        protected void deleteSuccess() {
            if (flow != null && flow.getId().equals(flowToDelete.getId())) {
                closeEditor();
            }
            dispatchEvent(new ShowInfoEvent("Flow '" + flowToDelete.getDefaultedLabel() + "' deleted."));
            dispatchEvent(new FlowDeletedEvent(flowToDelete));
        }

        protected void deleteFailure(RequestFailure requestFailure) {
            if (requestFailure.statusCode == 409) {
                dispatchEvent(new ShowFailureEvent(
                    "Flow '" + flowToDelete.getLabel() + "' can't be deleted, stopping it failed or it's still in use by other flows.",
                    5000
                ));
            } else {
                dispatchEvent(new RequestFailureEvent(requestFailure));
            }
        }
    }

    protected class AddNode implements Consumer<Node> {

        final double positionX;
        final double positionY;
        final boolean transformPosition;

        public AddNode(double positionX, double positionY, boolean transformPosition) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.transformPosition = transformPosition;
        }

        @Override
        public void accept(Node node) {
            flow.addNode(node);
            dispatchEvent(new FlowModifiedEvent(flow, true));

            if (flowDesigner != null) {
                if (transformPosition) {

                    // Correct the position so it feels like you are dropping in the middle of the patch header
                    double correctedX = Math.max(0, positionX - FlowDesignerConstants.PATCH_MIN_WIDTH / 2);
                    double correctedY = Math.max(0, positionY - (PATCH_LABEL_FONT_SIZE + PATCH_TITLE_FONT_SIZE + PATCH_PADDING * 2) / 2);

                    // Calculate the offset with the current transform (zoom, panning)
                    // TODO If I would know maths, I could probably do this with the transform matrices
                    Transform currentTransform = flowDesignerPanel.getViewport().getAbsoluteTransform();
                    double x = (correctedX - currentTransform.getTranslateX()) * currentTransform.getInverse().getScaleX();
                    double y = (correctedY - currentTransform.getTranslateY()) * currentTransform.getInverse().getScaleY();
                    node.getEditorSettings().setPositionX(x);
                    node.getEditorSettings().setPositionY(y);
                }

                LOG.debug("Adding node shape to flow designer: " + node);
                flowDesigner.addNodeShape(node);

                flowDesigner.selectNodeShape(node);
                dispatchEvent(new NodeSelectedEvent(node));
            }
        }
    }

    protected class DeleteNode implements Callback {

        final Node node;
        final boolean isWiringNode;

        public DeleteNode(Node node) {
            this.node = node;
            this.isWiringNode = node.isOfTypeConsumerOrProducer() && flow.hasDirectWiredSuperDependencies();
        }

        @Override
        public void call() {
            dispatchEvent("#flowNode", new FlowNodeCloseEvent());

            flow.removeNode(node);

            if (isWiringNode) { // Let's see if there is anything broken now, update dependencies tree
                Flow dupe = copyFlow();
                new UpdateDependencies(dupe, false, () -> {
                    if (flow != null && flow.getId().equals(dupe.getId())) {
                        flow.setSuperDependencies(dupe.getSuperDependencies());
                        flow.setSubDependencies(dupe.getSubDependencies());
                        setDependencies();
                    }
                    dispatchEvent(new ShowInfoEvent("Reloaded dependencies of flow '" + flow.getDefaultedLabel() + "'"));
                    dispatchEvent(new FlowModifiedEvent(flow, true));
                }).call();
            } else {
                dispatchEvent(new FlowModifiedEvent(flow, true));
            }

            if (flowDesigner != null) {
                LOG.debug("Removing node shape from flow designer: " + node);
                flowDesigner.deleteNodeShape(node);
            }
        }
    }

    protected class CreateNode implements Callback {

        final String currentFlowId;
        final String nodeType;
        final Consumer<Node> success;

        public CreateNode(String currentFlowId, String nodeType, Consumer<Node> success) {
            this.currentFlowId = currentFlowId;
            this.nodeType = nodeType;
            this.success = success;
        }

        @Override
        public void call() {
            LOG.debug("Creating node in flow designer: " + nodeType);
            sendRequest(
                false, true,
                resource("catalog", "node", nodeType).get(),
                new ObjectResponseCallback<Node>("Create node", NODE_CODEC) {
                    @Override
                    protected void onResponse(Node node) {
                        if (flow != null && flow.getId().equals(currentFlowId)) {
                            success.accept(node);
                        }
                    }
                }
            );
        }
    }

    protected class DuplicateNode implements Callback {

        final String currentFlowId;
        final Node node;

        public DuplicateNode(String currentFlowId, Node node) {
            this.currentFlowId = currentFlowId;
            this.node = node;
        }

        @Override
        public void call() {
            sendRequest(
                false, true,
                resource("flow", "duplicate", "node").post().json(NODE_CODEC.encode(node)),
                new ObjectResponseCallback<Node>("Duplicate node", NODE_CODEC) {
                    @Override
                    protected void onResponse(Node dupe) {
                        if (flow != null && flow.getId().equals(currentFlowId)) {
                            dupe.getEditorSettings().setPositionX(dupe.getEditorSettings().getPositionX() + 20);
                            dupe.getEditorSettings().setPositionY(dupe.getEditorSettings().getPositionY() + 20);
                            new AddNode(
                                dupe.getEditorSettings().getPositionX(),
                                dupe.getEditorSettings().getPositionY(),
                                false
                            ).accept(dupe);
                        }
                    }
                }
            );
        }
    }

    protected class CreateSubflowNode implements Callback {

        final String currentFlowId;
        final String subflowId;
        final Consumer<Node> success;

        public CreateSubflowNode(String currentFlowId, String subflowId, Consumer<Node> success) {
            this.currentFlowId = currentFlowId;
            this.subflowId = subflowId;
            this.success = success;
        }

        @Override
        public void call() {
            LOG.debug("Creating subflow in flow designer: " + subflowId);
            sendRequest(
                false, true,
                resource("flow", subflowId, "subflow").get(),
                new ObjectResponseCallback<Node>("Create subflow node", NODE_CODEC) {
                    @Override
                    protected void onResponse(Node subflowNode) {
                        createSuccess(subflowNode);
                    }
                }
            );
        }

        protected void createSuccess(Node subflowNode) {
            if (flow != null && flow.getId().equals(currentFlowId)) {
                // Add the subflow node to a temporary copy so we don't affect the
                // flow we are editing until we are done with resolution
                Flow flowCopy = copyFlow();
                if (subflowNode != null)
                    flowCopy.addNode(subflowNode);

                new UpdateDependencies(flowCopy, false, () -> {
                    if (flow != null && flow.getId().equals(currentFlowId)) {
                        flow.setSuperDependencies(flowCopy.getSuperDependencies());
                        flow.setSubDependencies(flowCopy.getSubDependencies());
                        setDependencies();
                    }
                    success.accept(subflowNode);
                }).call();
            }
        }
    }

    protected class UpdateDependencies implements Callback {

        final Flow flowToUpdate;
        final boolean hydrateSubs;
        final Callback success;

        public UpdateDependencies(Flow flowToUpdate, boolean hydrateSubs, Callback success) {
            this.flowToUpdate = flowToUpdate;
            this.hydrateSubs = hydrateSubs;
            this.success = success;
        }

        @Override
        public void call() {
            // Don't send dependencies to the server
            flowToUpdate.clearDependencies();

            LOG.debug("Resolving and updating dependencies: " + flowToUpdate);
            sendRequest(
                false, false,
                resource("flow", "resolve")
                    .addQueryParam("hydrateSubs", Boolean.toString(hydrateSubs))
                    .post().json(FLOW_CODEC.encode(flowToUpdate)),
                new ObjectResponseCallback<Flow>("Get flow dependencies", FLOW_CODEC) {
                    @Override
                    protected void onResponse(Flow resolvedFlow) {
                        flowToUpdate.setSubDependencies(resolvedFlow.getSubDependencies());
                        flowToUpdate.setSuperDependencies(resolvedFlow.getSuperDependencies());
                        success.call();
                    }

                    @Override
                    public void onFailure(RequestFailure requestFailure) {
                        super.onFailure(requestFailure);
                        dependencyFailure(requestFailure);
                    }
                }
            );
        }

        protected void dependencyFailure(RequestFailure requestFailure) {
            if (requestFailure.statusCode == 400) {
                dispatchEvent(new ShowFailureEvent(
                    "Can't create an endless loop between flows.",
                    5000
                ));
            } else {
                dispatchEvent(new RequestFailureEvent(requestFailure));
            }
        }

    }

    protected class CheckDependencies implements Callback {

        final Flow flowToCheck;
        final boolean isDelete;
        final Consumer<String> consumer;

        public CheckDependencies(Flow flowToCheck, boolean isDelete, Consumer<String> consumer) {
            this.flowToCheck = flowToCheck;
            this.isDelete = isDelete;
            this.consumer = consumer;
        }

        @Override
        public void call() {
            // Find out what's broken and build a text that can be shown to the user
            new UpdateDependencies(flowToCheck, false, () -> {
                FlowDependency[] superDependencies = flowToCheck.getDirectSuperDependencies();

                StringBuilder affectedFlows = new StringBuilder();
                for (FlowDependency superDependency : superDependencies) {
                    if (isDelete  || superDependency.isPeersInvalid()) {
                        affectedFlows.append(" ");
                        affectedFlows.append(superDependency.getDefaultedLabel().toUpperCase(Locale.ROOT));
                        affectedFlows.append(",");
                    }
                }
                if (affectedFlows.length() > 0) {
                    affectedFlows.deleteCharAt(affectedFlows.length() - 1);
                    if (superDependencies.length > 0) {
                        consumer.accept("The following flows will be stopped and automatically changed:" + affectedFlows);
                    }
                } else {
                    consumer.accept(null);
                }
            }).call();
        }
    }
}
