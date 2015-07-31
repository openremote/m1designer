package org.openremote.beta.shared.inventory;

import org.openremote.beta.shared.model.Identifier;

public class Actuator extends InventoryObject {

    protected Actuator() {
    }

    public Actuator(String label, Identifier id, Identifier... getSecondaryIds) {
        super(label, id, getSecondaryIds);
    }
}
