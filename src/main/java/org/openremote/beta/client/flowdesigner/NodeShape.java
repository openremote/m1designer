package org.openremote.beta.client.flowdesigner;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;

import static com.ait.lienzo.shared.core.types.ColorName.*;
import static java.lang.Math.min;
import static org.openremote.beta.client.flowdesigner.FlowDesignerConstants.*;
import static org.openremote.beta.shared.flow.Node.*;
import static org.openremote.beta.shared.util.Util.getDouble;
import static org.openremote.beta.shared.util.Util.getMap;

public abstract class NodeShape extends Box {

    static Color SENSOR_ACTUATOR_COLOR = new Color(102, 0, 146);
    static Color VIRTUAL_COLOR = new Color(145, 145, 148);
    static Color CLIENT_COLOR = new Color(25, 118, 210);

    public static Color getNodeColor(Node node) {
        switch (node.getIdentifier().getType()) {
            case TYPE_SENSOR:
            case TYPE_ACTUATOR:
                return SENSOR_ACTUATOR_COLOR;
            case TYPE_CONSUMER:
            case TYPE_PRODUCER:
            case TYPE_SUBFLOW:
                return VIRTUAL_COLOR;
            case TYPE_CLIENT:
                return CLIENT_COLOR;
            default:
                return null;
        }
    }

    public static Color getNodeLabelColor(Node node) {
        switch (node.getIdentifier().getType()) {
            case TYPE_SENSOR:
            case TYPE_ACTUATOR:
                return WHITE.getColor();
            case TYPE_CONSUMER:
            case TYPE_PRODUCER:
            case TYPE_SUBFLOW:
                return WHITE.getColor();
            case TYPE_CLIENT:
                return WHITE.getColor();
            default:
                return null;
        }
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

        this.node = node;
        this.title = new Box(
            PATCH_TITLE_CORNER_RADIUS,
            PATCH_TITLE_COLOR,
            new TextLabel(
                Node.getTypeLabel(node.getIdentifier().getType()),
                FONT_FAMILY,
                PATCH_TITLE_FONT_SIZE,
                PATCH_TITLE_TEXT_COLOR
            ),
            PATCH_TITLE_PADDING
        );
        this.slots = slots;

        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);

        if (node.getProperties() == null) {
            throw new IllegalArgumentException("No properties on node: " + node);
        }
        setX(getDouble(getMap(getMap(node.getProperties()), "editor"), "x"));
        setY(getDouble(getMap(getMap(node.getProperties()), "editor"), "y"));

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
