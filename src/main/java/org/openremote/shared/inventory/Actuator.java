package org.openremote.shared.inventory;

import org.openremote.shared.model.Identifier;

public class Actuator extends InventoryObject {

    protected Actuator() {
    }

    public Actuator(String label, Identifier id, Identifier... getSecondaryIds) {
        super(label, id, getSecondaryIds);
    }
}
