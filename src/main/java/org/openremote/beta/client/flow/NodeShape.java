package org.openremote.beta.client.flow;

import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;

import java.util.logging.Logger;

import static java.lang.Math.min;
import static org.openremote.beta.client.flow.Constants.*;
import static org.openremote.beta.shared.util.Util.getDouble;
import static org.openremote.beta.shared.util.Util.getMap;

public class NodeShape extends Box {

    private static final Logger LOG = Logger.getLogger(NodeShape.class.getName());

    final protected Node node;
    final protected Box title;
    final protected Slots slots;

    public NodeShape(Node node, Slots slots) {
        super(
            PATCH_CORNER_RADIUS,
            PATCH_COLOR,
            new TextLabel(
                node.getLabel(),
                FONT_FAMILY,
                PATCH_LABEL_FONT_SIZE,
                PATCH_LABEL_TEXT_COLOR
            ),
            PATCH_PADDING
        );

        this.node = node;
        this.title = new Box(
            PATCH_TITLE_CORNER_RADIUS,
            PATCH_TITLE_COLOR,
            new TextLabel(
                node.getType(),
                FONT_FAMILY,
                PATCH_TITLE_FONT_SIZE,
                PATCH_TITLE_TEXT_COLOR
            ),
            PATCH_TITLE_PADDING
        );
        this.slots = slots;

        setDraggable(true);
        setDragMode(DragMode.SAME_LAYER);

        setX(getDouble(getMap(getMap(node.getProperties()), "editor"), "x"));
        setY(getDouble(getMap(getMap(node.getProperties()), "editor"), "y"));

        // This sets a minimum width to title width plus some padding
        setWidth(title.getWidth() + PATCH_PADDING * 4);

        // Set the slots and the height depending on slot size
        slots.setSlots(getWidth(), node.getSlots());
        setHeight(slots.getHeight());

        // Position title in this group
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

        add(title);
    }

    public Slots getSlots() {
        return slots;
    }
}
