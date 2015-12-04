/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.shell.floweditor;

import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental.dom.Element;
import elemental.js.util.JsMapFromStringToString;
import jsinterop.annotations.JsType;
import org.openremote.client.event.*;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.JsUtil;
import org.openremote.client.shared.View;
import org.openremote.shared.event.Message;
import org.openremote.shared.event.client.*;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.client.shell.floweditor.FlowDesignerConstants.*;

@JsType
public class FlowEditorPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowEditorPresenter.class);

    public Flow flow;

    protected FlowDesigner flowDesigner;
    protected LienzoPanel flowDesignerPanel;
    protected Transform flowDesignerInitialTransform;

    public FlowEditorPresenter(View view) {
        super(view);

        Window.addResizeHandler(event -> onContainerResize());

        addListener(FlowEditEvent.class, event -> {
            flow = event.getFlow();
            notifyPath("flow");
            startFlowDesigner();
        });

        addListener(FlowDeletedEvent.class, event -> {
            if (event.matches(flow)) {
                flow = null;
                notifyPathNull("flow");
                stopFlowDesigner();
            }
        });

        addListener(NodeSelectedEvent.class, event -> {
            if (flowDesigner != null && flow != null) {
                Node node = flow.findNode(event.getNodeId());
                if (node != null) {
                    flowDesigner.selectNodeShape(node);
                }
            }
        });

        addListener(NodeAddedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                Node node = event.getNode();

                if (event.isTransformPosition()) {
                    // Correct the position so it feels like you are dropping in the middle of the patch header
                    double correctedX = Math.max(0, event.getPositionX() - FlowDesignerConstants.PATCH_MIN_WIDTH / 2);
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

                dispatch(new NodeSelectedEvent(node.getId()));
            }
        });

        addListener(NodeDeletedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                Node node = event.getNode();
                LOG.debug("Removing node shape from flow designer: " + node);
                flowDesigner.deleteNodeShape(node);
            }
        });

        addListener(NodeModifiedEvent.class, event -> {
            if (flowDesigner != null && event.matches(flow)) {
                flowDesigner.updateNodeShape(event.getNode());
            }
        });

        addListener(MessageReceivedEvent.class, event -> {
            Message message = event.getMessage();
            if (flowDesigner != null && flow != null && flow.findSlot(message.getSlotId()) != null) {
                flowDesigner.handleMessage(message);
            }
        });

        addListener(MessageSendEvent.class, event -> {
            Message message = event.getMessage();
            if (flowDesigner != null && flow != null && flow.findSlot(message.getSlotId()) != null) {
                flowDesigner.handleMessage(message);
            }
        });
    }

    public void onContainerResize() {
        if (flowDesignerPanel != null) {
            Element container = JsUtil.asElementalElement(getRequiredElement("#flowDesigner"));
            flowDesignerPanel.setPixelSize(container.getClientWidth(), container.getClientHeight());
        }
    }

    public void onDrop(String label, String nodeType,
                       String subflowId,
                       String sensorEndpoint, String discoveryEndpoint, String actuatorEndpoint,
                       double positionX, double positionY) {
        // TODO ugly
        JsMapFromStringToString nodeProperties = JsMapFromStringToString.create();
        if (sensorEndpoint != null && sensorEndpoint.length() > 0) {
            nodeType = "urn:openremote:flow:node:sensor";
            nodeProperties.put("sensorEndpoint", sensorEndpoint);
            if (discoveryEndpoint != null && discoveryEndpoint.length() > 0) {
                nodeProperties.put("discoveryEndpoint", discoveryEndpoint);
            }
        } else if (actuatorEndpoint != null && actuatorEndpoint.length() > 0) {
            nodeType = "urn:openremote:flow:node:actuator";
            nodeProperties.put("actuatorEndpoint", actuatorEndpoint);
        }

        if (nodeType != null && nodeType.length() > 0) {
            dispatch(new NodeCreateEvent(
                flow,
                label,
                nodeType,
                JsonUtils.stringify(nodeProperties),
                positionX,
                positionY,
                false
            ));
        } else if (subflowId != null && subflowId.length() > 0) {
            dispatch(new SubflowNodeCreateEvent(flow, subflowId, positionX, positionY));
        }
    }

    protected void startFlowDesigner() {

        stopFlowDesigner();

        if (flowDesignerPanel == null) {
            Element container = JsUtil.asElementalElement(getRequiredElement("#flowDesigner"));
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
                dispatch(new NodeSelectedEvent(node.getId()));
            }

            @Override
            protected void onMoved(Node node) {
                dispatch(new FlowModifiedEvent(flow, false));
            }

            @Override
            protected void onAddition(Wire wire) {
                dispatch(new FlowModifiedEvent(flow, true));
            }

            @Override
            protected void onRemoval(Wire wire) {
                dispatch(new FlowModifiedEvent(flow, true));
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
