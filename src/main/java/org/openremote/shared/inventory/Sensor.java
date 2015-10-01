package org.openremote.shared.inventory;

import org.openremote.shared.model.Identifier;

public class Sensor extends InventoryObject {

    protected Sensor() {
    }

    public Sensor(String label, Identifier primary, Identifier... secondaries) {
        super(label, primary, secondaries);
    }
}
