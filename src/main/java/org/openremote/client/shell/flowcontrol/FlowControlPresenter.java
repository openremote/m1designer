package org.openremote.client.shell.flowcontrol;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.http.client.Response;
import elemental.js.util.JsMapFromStringTo;
import org.openremote.client.event.*;
import org.openremote.client.shared.*;
import org.openremote.client.shared.request.RequestFailure;
import org.openremote.client.shared.request.RequestFailureEvent;
import org.openremote.client.shared.request.RequestPresenter;
import org.openremote.client.shared.session.SessionOpenedEvent;
import org.openremote.client.shared.session.event.ServerSendEvent;
import org.openremote.client.shell.FlowCodec;
import org.openremote.client.shell.NodeCodec;
import org.openremote.shared.event.*;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowDependency;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static org.openremote.client.shared.Timeout.debounce;

@JsExport
@JsType
public class FlowControlPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowControlPresenter.class);

    @JsType
    public interface FlowControlView extends Component {
        void toggleFlowControl();
    }

    private static final FlowCodec FLOW_CODEC = GWT.create(FlowCodec.class);
    private static final NodeCodec NODE_CODEC = GWT.create(NodeCodec.class);

    public Flow flow;
    public String flowControlTitle;
    public boolean flowDirty = false;
    public boolean flowUnsaved;
    public FlowStatusDetail flowStatusDetail;
    public FlowDependency[] flowSuperDependencies;
    public FlowDependency[] flowSubDependencies;
    public String selectedNodeId;

    public FlowControlPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addListener(ShortcutEvent.class, event -> {
            if (flow == null)
                return;
            if (event.getKey() == 82) {
                ((FlowControlView) getViewComponent()).toggleFlowControl();
            } else if (event.getKey() == 83) {
                redeployFlow();
            }
        });

        addPrepareListener(ShellCloseEvent.class, this::vetoIfDirty);

        addPrepareListener(FlowEditEvent.class, this::vetoIfDirty);

        addListener(FlowEditEvent.class, event -> {

            flow = event.getFlow();
            notifyPath("flow");

            setFlowUnsaved(event.isUnsaved());
            setFlowDirty(flowUnsaved);

            setDependencies();

            setFlowStatusDetail(new FlowStatusDetail("REQUESTING FLOW STATUS..."));
            dispatch(new ServerSendEvent(new FlowRequestStatusEvent(flow.getId())));

            sendConsoleRefresh(flow);
        });

        addListener(FlowDeletedEvent.class, event -> {
            flow = null;
            notifyPathNull("flow");
            sendConsoleRefresh(null);
        });

        addListener(NodeSelectedEvent.class, event -> {
            selectedNodeId = event.getNodeId();
            notifyPath("selectedNodeId");
        });

        addListener(SessionOpenedEvent.class, event -> {
            dispatch(new ServerSendEvent(new FlowRequestStatusEvent()));
        });

        addListener(FlowStatusEvent.class, event -> {
            if (event.matches(flow)) {
                setFlowStatusDetail(new FlowStatusDetail(event.getPhase()));
            }
        });

        addListener(FlowDeploymentFailureEvent.class, event -> {
            if (event.matches(flow)) {
                setFlowStatusDetail(new FlowStatusDetail(event));
            }
        });

        addListener(FlowRuntimeFailureEvent.class, event -> {
            // TODO should probably do more when a flow fails at runtime
            dispatch(new ShowFailureEvent(event.getMessage(), 10000));
        });

        addListener(FlowModifiedEvent.class, event -> {
            setFlowStatusDetail(flowStatusDetail);
            setFlowDirty(true);
            if (event.isNotifyConsole()) {
                sendConsoleRefresh(event.getFlow());
            }
        });

        addListener(NodeCreateEvent.class, event -> {
            if (event.matches(flow)) {
                new CreateNodeProcedure(
                    flow.getId(),
                    event.getNodeType(),
                    new AddNodeProcedure(
                        event.getPositionX(),
                        event.getPositionY(),
                        event.isApplyPositionAsProperties(),
                        true
                    )
                ).call();
            }
        });

        addListener(SubflowNodeCreateEvent.class, event -> {
            if (event.matches(flow)) {
                // Can't add current flow as subflow
                if (flow.getId().equals(event.getSubflowId())) {
                    return;
                }
                new CreateSubflowNodeProcedure(
                    flow.getId(),
                    event.getSubflowId(),
                    new AddNodeProcedure(
                        event.getPositionX(),
                        event.getPositionY(),
                        event.isApplyPositionAsProperties(),
                        true)
                ).call();
            }
        });

        addListener(NodeSelectedEvent.class, event -> {
            if (flow != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    dispatch(new NodeEditEvent(flow, node));
                }
            }
        });

        addListener(ConsoleWidgetModifiedEvent.class, event -> {
            if (flow != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    node.setProperties(event.getNodeProperties());
                    dispatch(new FlowModifiedEvent(flow, false));
                    if (node.getId().equals(selectedNodeId)) {
                        dispatch(new NodePropertiesRefreshEvent(node.getId()));
                    }
                }
            }
        });

        addListener(NodeDeleteEvent.class, event -> {
            new DeleteNodeProcedure(event.getNode()).call();
        });

        addListener(NodeDeletedEvent.class, event -> {
            if (event.getNode().getId().equals(selectedNodeId)) {
                selectedNodeId = null;
                notifyPathNull("selectedNodeId");
            }
        });

        addListener(NodeDuplicateEvent.class, event -> {
            if (event.matches(flow)) {
                new DuplicateNodeProcedure(flow.getId(), event.getNode()).call();
            }
        });
    }

    /* ################################################################################# */

    public void flowPropertyChanged() {
        flowDirty = true; // Assume this is dirty, now other listeners might run
        debounce("FlowPropertyChange", () -> {
            if (flowDirty) { // If it's still dirty (after other listeners executed), tell the user
                dispatch(new FlowModifiedEvent(flow, false));
            }
        }, 500);
    }

    public void editFlowDependency(Flow dependency) {
        dispatch(new FlowLoadEvent(dependency.getId()));
    }

    public void startFlow() {
        dispatch(new ServerSendEvent(new FlowDeployEvent(flow.getId())));
    }

    public void stopFlow() {
        dispatch(new ServerSendEvent(new FlowStopEvent(flow.getId())));
    }

    public void exportFlow() {
        Flow dupe = copyFlow();
        new UpdateDependenciesProcedure(dupe, true, () -> {
            LOG.info(FLOW_CODEC.encode(dupe).toString());
            dispatch(new ShowInfoEvent("Export complete, see your browsers console."));
        }).call();
    }

    public void redeployFlow() {
        new SaveFlowProcedure(copyFlow(), flowUnsaved, savedFlow -> {
            dispatch(new ServerSendEvent(new FlowStopEvent(savedFlow.getId())));
            dispatch(new ServerSendEvent(new FlowDeployEvent(savedFlow.getId())));
        }).call();
    }

    public void saveFlow() {
        new SaveFlowProcedure(copyFlow(), flowUnsaved).call();
    }

    public void deleteFlow() {
        new DeleteFlowProcedure(copyFlow()).call();
    }

    /* ################################################################################# */

    protected void vetoIfDirty(Event event) {
        if (flowDirty) {
            dispatchDirtyConfirmation(() -> {
                setFlowDirty(false);
                dispatch(event);
            });
            throw new VetoEventException();
        }
    }

    protected void dispatchDirtyConfirmation(Callback confirmAction) {
        dispatch(new ConfirmationEvent(
            "Unsaved Changes",
            "You have edited the current flow and not redeployed/saved the changes. Continue without saving changes?",
            confirmAction
        ));
    }

    protected Flow copyFlow() {
        return FLOW_CODEC.decode(FLOW_CODEC.encode(flow));
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

    protected void sendConsoleRefresh(Flow flow) {
        if (flow == null) {
            dispatch(new ConsoleRefreshEvent());
            return;
        }

        Flow dupe = copyFlow();
        new UpdateDependenciesProcedure(
            dupe,
            true,
            () -> Timeout.debounce(
                "Console Refresh",
                () -> dispatch(new ConsoleRefreshEvent(dupe, selectedNodeId)),
                20
            )
        ).call();
    }

    /* ################################################################################# */

    protected class AddNodeProcedure implements Consumer<Node> {

        final double positionX;
        final double positionY;
        final boolean applyPositionAsProperties; // Or editor settings
        final boolean transformPosition;

        public AddNodeProcedure(double positionX, double positionY, boolean applyPositionAsProperties, boolean transformPosition) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.applyPositionAsProperties = applyPositionAsProperties;
            this.transformPosition = transformPosition;
        }

        @Override
        public void accept(Node node) {
            // TODO this is a bit awkward, we might want to clean this up along with the zoom factor/transformposition
            if (applyPositionAsProperties) {
                JsMapFromStringTo existingProperties = JsonUtils.safeEval(node.getProperties());
                existingProperties.put("positionX", positionX);
                existingProperties.put("positionY", positionY);
                String nodeProperties = JsonUtils.stringify(existingProperties);
                node.setProperties(nodeProperties);
            }

            flow.addNode(node);
            dispatch(new FlowModifiedEvent(flow, true));

            if (applyPositionAsProperties) {
                // TODO what's the default location on the editor canvas when you drop it on the console?
                dispatch(new NodeAddedEvent(flow, node, 250, 250, true));
            } else {
                dispatch(new NodeAddedEvent(flow, node, positionX, positionY, transformPosition));
            }
        }
    }

    protected class CreateNodeProcedure implements Callback {

        final String currentFlowId;
        final String nodeType;
        final Consumer<Node> success;

        public CreateNodeProcedure(String currentFlowId, String nodeType, Consumer<Node> success) {
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

    protected class DeleteNodeProcedure implements Callback {

        final Node node;
        final boolean isWiringNode;

        public DeleteNodeProcedure(Node node) {
            this.node = node;
            this.isWiringNode = node.isOfTypeConsumerOrProducer() && flow.hasDirectWiredSuperDependencies();
        }

        @Override
        public void call() {
            flow.removeNode(node);

            if (isWiringNode) { // Let's see if there is anything broken now, update dependencies tree
                Flow dupe = copyFlow();
                new UpdateDependenciesProcedure(dupe, false, () -> {
                    if (flow != null && flow.getId().equals(dupe.getId())) {
                        flow.setSuperDependencies(dupe.getSuperDependencies());
                        flow.setSubDependencies(dupe.getSubDependencies());
                        setDependencies();
                    }
                    dispatch(new ShowInfoEvent("Reloaded dependencies of flow '" + flow.getDefaultedLabel() + "'"));
                    dispatch(new FlowModifiedEvent(flow, true));
                    dispatch(new NodeDeletedEvent(flow, node));
                }).call();
            } else {
                dispatch(new FlowModifiedEvent(flow, true));
                dispatch(new NodeDeletedEvent(flow, node));
            }
        }
    }

    protected class DuplicateNodeProcedure implements Callback {

        final String currentFlowId;
        final Node node;

        public DuplicateNodeProcedure(String currentFlowId, Node node) {
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
                            new AddNodeProcedure(
                                dupe.getEditorSettings().getPositionX(),
                                dupe.getEditorSettings().getPositionY(),
                                false,
                                false
                            ).accept(dupe);
                        }
                    }
                }
            );
        }
    }

    protected class CreateSubflowNodeProcedure implements Callback {

        final String currentFlowId;
        final String subflowId;
        final Consumer<Node> success;

        public CreateSubflowNodeProcedure(String currentFlowId, String subflowId, Consumer<Node> success) {
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

                new UpdateDependenciesProcedure(flowCopy, false, () -> {
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

    protected class SaveFlowProcedure implements Callback {

        final Flow flowToSave;
        final boolean flowIsUnsaved;
        final Consumer<Flow> success;

        public SaveFlowProcedure(Flow flowToSave, boolean flowIsUnsaved) {
            this(flowToSave, flowIsUnsaved, null);
        }

        public SaveFlowProcedure(Flow flowToSave, boolean flowIsUnsaved, Consumer<Flow> success) {
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
                        new RequestPresenter.StatusResponseCallback("Save new flow", 201) {
                            @Override
                            protected void onResponse(Response response) {
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
                        new RequestPresenter.StatusResponseCallback("Save flow", 204) {
                            @Override
                            protected void onResponse(Response response) {
                                saveSuccess();
                                if (success != null)
                                    success.accept(flowToSave);
                            }
                        }
                    );
                };
            }

            new CheckDependenciesProcedure(flowToSave, false, affectedFlows -> {
                if (affectedFlows == null) {
                    saveCallback.call();
                } else {
                    String confirmationText =
                        "Are you sure you want to save changes to flow '" +
                            flowToSave.getDefaultedLabel() +
                            "'? " + affectedFlows;
                    dispatch(
                        new ConfirmationEvent("Save Breaking Changes", confirmationText, saveCallback)
                    );
                }
            }).call();
        }

        protected void saveSuccess() {
            if (flow != null && flow.getId().equals(flowToSave.getId())) {
                setFlowUnsaved(false);
                setFlowDirty(false);
                sendConsoleRefresh(flow);
            }
            dispatch(new FlowSavedEvent(flowToSave));
            dispatch(new ShowInfoEvent("Flow '" + flowToSave.getDefaultedLabel() + "' saved."));
        }
    }

    protected class DeleteFlowProcedure implements Callback {

        final Flow flowToDelete;

        public DeleteFlowProcedure(Flow flowToDelete) {
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

            new CheckDependenciesProcedure(flowToDelete, true, affectedFlows -> {

                if (affectedFlows != null)
                    confirmationText.append(" ").append(affectedFlows);

                dispatch(new ConfirmationEvent(
                    "Remove Flow",
                    confirmationText.toString(),
                    new Callback() {
                        @Override
                        public void call() {
                            sendRequest(false, false,
                                resource("flow", flowToDelete.getId()).delete(),
                                new RequestPresenter.StatusResponseCallback("Delete flow", 204) {
                                    @Override
                                    protected void onResponse(Response response) {
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
            dispatch(new ShowInfoEvent("Flow '" + flowToDelete.getDefaultedLabel() + "' deleted."));
            dispatch(new FlowDeletedEvent(flowToDelete));
        }

        protected void deleteFailure(RequestFailure requestFailure) {
            if (requestFailure.statusCode == 409) {
                dispatch(new ShowFailureEvent(
                    "Flow '" + flowToDelete.getLabel() + "' can't be deleted, stopping it failed or it's still in use by other flows.",
                    5000
                ));
            } else {
                dispatch(new RequestFailureEvent(requestFailure));
            }
        }
    }

    protected class UpdateDependenciesProcedure implements Callback {

        final Flow flowToUpdate;
        final boolean hydrateSubs;
        final Callback success;

        public UpdateDependenciesProcedure(Flow flowToUpdate, boolean hydrateSubs, Callback success) {
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
            if (requestFailure.statusCode == 409) {
                dispatch(new ShowFailureEvent(
                    "Can't create an endless loop between flows.",
                    5000
                ));
            } else {
                dispatch(new RequestFailureEvent(requestFailure));
            }
        }

    }

    protected class CheckDependenciesProcedure implements Callback {

        final Flow flowToCheck;
        final boolean isDelete;
        final Consumer<String> consumer;

        public CheckDependenciesProcedure(Flow flowToCheck, boolean isDelete, Consumer<String> consumer) {
            this.flowToCheck = flowToCheck;
            this.isDelete = isDelete;
            this.consumer = consumer;
        }

        @Override
        public void call() {
            // Find out what's broken and build a text that can be shown to the user
            new UpdateDependenciesProcedure(flowToCheck, false, () -> {
                FlowDependency[] superDependencies = flowToCheck.getDirectSuperDependencies();

                StringBuilder affectedFlows = new StringBuilder();
                for (FlowDependency superDependency : superDependencies) {
                    if (isDelete || superDependency.isPeersInvalid()) {
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
