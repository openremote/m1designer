package org.openremote.beta.client.flowdesigner;

import com.ait.lienzo.shared.core.types.DragMode;
import org.openremote.beta.shared.flow.Node;

import static java.lang.Math.min;
import static org.openremote.beta.client.flowdesigner.Constants.*;
import static org.openremote.beta.shared.util.Util.getDouble;
import static org.openremote.beta.shared.util.Util.getMap;

public abstract class NodeShape extends Box {

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
