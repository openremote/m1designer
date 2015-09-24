package org.openremote.beta.client.shell.floweditor;

import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shell.event.*;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.beta.client.shell.floweditor.FlowDesignerConstants.*;

@JsExport
@JsType
public class FlowEditorPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    public Flow flow;

    protected FlowDesigner flowDesigner;
    protected LienzoPanel flowDesignerPanel;
    protected Transform flowDesignerInitialTransform;

    public FlowEditorPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        Window.addResizeHandler(event -> onContainerResize());

        addEventListener(FlowEditEvent.class, event -> {
            flow = event.getFlow();
            notifyPath("flow");
            startFlowDesigner();
        });

        addEventListener(FlowDeletedEvent.class, event-> {
            if (event.matches(flow)) {
                flow = null;
                notifyPathNull("flow");
                stopFlowDesigner();
            }
        });

        addEventListener(NodeSelectEvent.class, event -> {
            if (flowDesigner != null && flow != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    flowDesigner.selectNodeShape(node);
                    dispatchEvent(new NodeSelectedEvent(flow, node));
                }
            }
        });

        addEventListener(NodeAddedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                Node node = event.getNode();

                if (event.isTransformPosition()) {
                    // Correct the position so it feels like you are dropping in the middle of the patch header
                    double correctedX = Math.max(0, event.getPositionX()- FlowDesignerConstants.PATCH_MIN_WIDTH / 2);
                    double correctedY = Math.max(0, event.getPositionY() - (PATCH_LABEL_FONT_SIZE + PATCH_TITLE_FONT_SIZE + PATCH_PADDING * 2) / 2);

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
                dispatchEvent(new NodeSelectedEvent(flow, node));
            }
        });

        addEventListener(NodeDeletedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                Node node = event.getNode();
                LOG.debug("Removing node shape from flow designer: " + node);
                flowDesigner.deleteNodeShape(node);
            }
        });

        addEventListener(NodeModifiedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                flowDesigner.updateNodeShape(event.getNode());
            }
        });

        addEventListener(Message.class, message -> {
            if (flowDesigner != null && flow != null && flow.findSlot(message.getSlotId()) != null) {
                flowDesigner.handleMessage(message);
            }
        });
    }

    public void onContainerResize() {
        if (flowDesignerPanel != null) {
            Element container = getRequiredElement("#flowDesigner");
            flowDesignerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight());
        }
    }

    public void onDrop(String nodeType, String subflowId, double positionX, double positionY) {
        if (nodeType != null) {
            dispatchEvent(new NodeCreateEvent(flow, nodeType, positionX, positionY));
        } else if (subflowId != null) {
            dispatchEvent(new SubflowNodeCreateEvent(flow, subflowId, positionX, positionY));
        }
    }

    protected void startFlowDesigner() {

        stopFlowDesigner();

        if (flowDesignerPanel == null) {
            Element container = getRequiredElement("#flowDesigner");
            this.flowDesignerPanel = new LienzoPanel();

            flowDesignerPanel.setSelectCursor(Style.Cursor.MOVE);

            onContainerResize();

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

        flowDesignerPanel.getViewport().setTransform(flowDesignerInitialTransform);

        flowDesigner = new FlowDesigner(flow, flowDesignerPanel.getScene()) {
            @Override
            protected void onSelection(Node node) {
                dispatchEvent(new NodeSelectedEvent(flow, node));
            }

            @Override
            protected void onMoved(Node node) {
                dispatchEvent(new FlowModifiedEvent(flow, true));
            }

            @Override
            protected void onAddition(Wire wire) {
                dispatchEvent(new FlowModifiedEvent(flow, true));
            }

            @Override
            protected void onRemoval(Wire wire) {
                dispatchEvent(new FlowModifiedEvent(flow, true));
            }
        };
        flowDesignerPanel.draw();
    }

    protected void stopFlowDesigner() {
        if (flowDesignerPanel != null) {
            flowDesignerPanel.getScene().removeAll();
            flowDesigner = null;
        }
    }

}
