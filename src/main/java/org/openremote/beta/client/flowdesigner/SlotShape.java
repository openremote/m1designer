package org.openremote.beta.client.flowdesigner;

import org.openremote.beta.shared.flow.Slot;

import static org.openremote.beta.client.flowdesigner.Constants.*;

public class SlotShape extends Box {

    final protected Slot slot;

    public SlotShape(Slot slot) {
        super(
            SLOT_CORNER_RADIUS,
            slot.isOfType(Slot.TYPE_SINK) ? SLOT_SINK_COLOR : SLOT_SOURCE_COLOR,
            new TextLabel(
                slot.getLabel() != null && slot.getLabel().length() > 0 ? slot.getLabel() : TextLabel.space(SLOT_PADDING),
                FONT_FAMILY,
                SLOT_FONT_SIZE,
                slot.isOfType(Slot.TYPE_SINK) ? SLOT_SINK_TEXT_COLOR : SLOT_SOURCE_TEXT_COLOR
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
            setFillColor(getSlot().isOfType(Slot.TYPE_SINK) ? SLOT_SINK_ATTACHED_COLOR : SLOT_SOURCE_ATTACHED_COLOR);
        } else {
            setFillColor((getSlot().isOfType(Slot.TYPE_SINK) ? SLOT_SINK_COLOR : SLOT_SOURCE_COLOR));
        }
    }

    @Override
    public Slots getParent() {
        return (Slots)super.getParent();
    }
}
