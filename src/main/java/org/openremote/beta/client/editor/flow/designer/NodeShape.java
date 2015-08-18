package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.model.Properties;

import static com.ait.lienzo.shared.core.types.ColorName.WHITE;
import static java.lang.Math.min;
import static org.openremote.beta.client.editor.flow.designer.FlowDesignerConstants.*;
import static org.openremote.beta.shared.flow.Node.*;
import static org.openremote.beta.shared.model.PropertyDescriptor.TYPE_DOUBLE;

public abstract class NodeShape extends Box {

    static Color SENSOR_ACTUATOR_COLOR = new Color(102, 0, 146);
    static Color VIRTUAL_COLOR = new Color(145, 145, 148);
    static Color CLIENT_COLOR = new Color(25, 118, 210);

    public static Color getNodeColor(Node node) {
        String color = Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_COLOR);
        switch (color != null ? NodeColor.valueOf(color) : NodeColor.DEFAULT) {
            case SENSOR_ACTUATOR:
                return SENSOR_ACTUATOR_COLOR;
            case VIRTUAL:
                return VIRTUAL_COLOR;
            case CLIENT:
                return CLIENT_COLOR;
            default:
                return null;
        }
    }

    public static Color getNodeLabelColor(Node node) {
        return WHITE.getColor();
    }

    protected Node node;
    protected Box title;
    protected Slots slots;

    public NodeShape(Node node, Slots slots) {
        super(
            PATCH_CORNER_RADIUS,
            getNodeColor(node) != null ? getNodeColor(node) : PATCH_COLOR,
            null,
            PATCH_PADDING
        );

        if (!node.hasProperties()) {
            throw new IllegalArgumentException("No properties on node: " + node);
        }

        initialize();

        // TODO: We don't dynamically update slots
        this.slots = slots;
        this.slots.setSlots(node.getSlots());

        setNode(node);
    }

    public Slots getSlots() {
        return slots;
    }

    public void setNode(Node node) {
        this.node = node;
        setTextLabel(
            new TextLabel(
                node.getLabel(),
                FONT_FAMILY,
                PATCH_LABEL_FONT_SIZE,
                getNodeLabelColor(node) != null ? getNodeLabelColor(node) : PATCH_LABEL_TEXT_COLOR
            )
        );
        setTitle();
        updateSizeAndPosition();
    }

    protected void updateSizeAndPosition() {
        setX(Properties.get(this.node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_X));
        setY(Properties.get(this.node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_Y));

        // This sets a minimum width to title width plus some padding
        setWidth(title.getWidth() + PATCH_PADDING * 4);
        setHeight(slots.getHeight());

        // Position slots on our coordinates
        slots.setX(getX());
        slots.setY(getY());
        // Distance between sink and source slots is width of node
        slots.setNodeWidth(getWidth());

        // Position title in this group
        title.centerHorizontal(this);
        title.setY(title.getY() - title.getHeight() + min(PATCH_TITLE_PADDING, PATCH_PADDING));
    }

    protected void setTitle() {
        if (this.title != null)
            remove(this.title);
        this.title = new Box(
            PATCH_TITLE_CORNER_RADIUS,
            PATCH_TITLE_COLOR,
            new TextLabel(
                Properties.get(this.node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL),
                FONT_FAMILY,
                PATCH_TITLE_FONT_SIZE,
                PATCH_TITLE_TEXT_COLOR
            ),
            PATCH_TITLE_PADDING
        );
        add(title);
    }

    protected void initialize() {
        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);

        // Update position slots (they are not in this group because we don't want them draggable)
        addNodeDragMoveHandler(event -> {
            this.slots.setX(getX());
            this.slots.setY(getY());

            this.node.getEditorProperties().put(EDITOR_PROPERTY_X, getX());
            this.node.getEditorProperties().put(EDITOR_PROPERTY_Y, getY());
        });

        addNodeDragEndHandler(event -> moved(this.node));

        new SelectionEventHandler(this) {
            @Override
            protected void onSelection() {
                selected(NodeShape.this.node);
            }
        };
    }

    protected abstract void selected(Node node);

    protected abstract void moved(Node node);

}
