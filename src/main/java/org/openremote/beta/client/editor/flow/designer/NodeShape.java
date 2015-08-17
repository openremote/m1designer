package org.openremote.beta.client.editor.flow.designer;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.model.Properties;
import org.openremote.beta.shared.model.PropertyDescriptor;

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

    final protected Node node;
    final protected Box title;
    final protected Slots slots;

    public NodeShape(Node node, Slots slots) {
        super(
            PATCH_CORNER_RADIUS,
            getNodeColor(node) != null ? getNodeColor(node) : PATCH_COLOR,
            new TextLabel(
                node.getLabel(),
                FONT_FAMILY,
                PATCH_LABEL_FONT_SIZE,
                getNodeLabelColor(node) != null ? getNodeLabelColor(node) : PATCH_LABEL_TEXT_COLOR
            ),
            PATCH_PADDING
        );

        if (!node.hasProperties()) {
            throw new IllegalArgumentException("No properties on node: " + node);
        }

        this.node = node;
        this.title = new Box(
            PATCH_TITLE_CORNER_RADIUS,
            PATCH_TITLE_COLOR,
            new TextLabel(
                Properties.get(node.getEditorProperties(), EDITOR_PROPERTY_TYPE_LABEL),
                FONT_FAMILY,
                PATCH_TITLE_FONT_SIZE,
                PATCH_TITLE_TEXT_COLOR
            ),
            PATCH_TITLE_PADDING
        );
        this.slots = slots;

        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);

        setX(Properties.get(node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_X));
        setY(Properties.get(node.getEditorProperties(), TYPE_DOUBLE, EDITOR_PROPERTY_Y));

        // This sets a minimum width to title width plus some padding
        setWidth(title.getWidth() + PATCH_PADDING * 4);

        // Set the slots and the height depending on slot number/size
        slots.setSlots(getWidth(), node.getSlots());
        setHeight(slots.getHeight());

        // Position title in this group
        add(title);
        title.centerHorizontal(this);
        title.setY(title.getY() - title.getHeight() + min(PATCH_TITLE_PADDING, PATCH_PADDING));

        // Initial position slots
        slots.setX(getX());
        slots.setY(getY());

        // Update position slots (they are not in this group because we don't want them draggable)
        addNodeDragMoveHandler(event -> {
            slots.setX(getX());
            slots.setY(getY());
        });

        new SelectionEventHandler(this) {
            @Override
            protected void onSelection() {
                selected(node);
            }
        };
    }

    public Slots getSlots() {
        return slots;
    }

    protected abstract void selected(Node node);

}
