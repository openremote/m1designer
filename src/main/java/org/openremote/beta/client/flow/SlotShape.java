package org.openremote.beta.client.flow;

import org.openremote.beta.shared.flow.Slot;

import static org.openremote.beta.client.flow.Constants.*;
import static org.openremote.beta.shared.flow.Slot.Type.SINK;

public class SlotShape extends Box {

    final protected Slot slot;

    public SlotShape(Slot slot) {
        super(
            SLOT_CORNER_RADIUS,
            (slot.getType() == SINK ? SLOT_SINK_COLOR : SLOT_SOURCE_COLOR),
            new TextLabel(
                slot.getLabel() != null && slot.getLabel().length() > 0 ? slot.getLabel() : TextLabel.space(SLOT_PADDING),
                FONT_FAMILY,
                SLOT_FONT_SIZE,
                (slot.getType() == SINK ? SLOT_SINK_TEXT_COLOR : SLOT_SOURCE_TEXT_COLOR)
            ),
            SLOT_PADDING
        );
        this.slot = slot;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setAttached(boolean attached) {
        if(attached) {
            setFillColor(getSlot().getType() == SINK ? SLOT_SINK_ATTACHED_COLOR : SLOT_SOURCE_ATTACHED_COLOR);
        } else {
            setFillColor((slot.getType() == SINK ? SLOT_SINK_COLOR : SLOT_SOURCE_COLOR));
        }
    }

    @Override
    public Slots getParent() {
        return (Slots)super.getParent();
    }
}
