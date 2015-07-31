package org.openremote.beta.shared.inventory;

import org.openremote.beta.shared.model.Identifier;

public class Sensor extends InventoryObject {

    protected Sensor() {
    }

    public Sensor(String label, Identifier primary, Identifier... secondaries) {
        super(label, primary, secondaries);
    }
}
