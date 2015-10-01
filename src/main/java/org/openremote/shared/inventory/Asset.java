package org.openremote.shared.inventory;

import org.openremote.shared.model.Identifier;

public class Asset extends InventoryObject {

    protected Device[] devices;

    protected Asset[] children;

    protected Asset() {
    }

    public Asset(String label, Identifier primary, Asset... children) {
        this(label, primary, new Identifier[0], new Device[0], children);
    }

    public Asset(String label, Identifier primary, Device[] devices, Asset... children) {
        this(label, primary, new Identifier[0], devices, children);
    }

    public Asset(String label, Identifier primary, Identifier[] secondaries, Asset... children) {
        this(label, primary, secondaries, new Device[0], children);
    }

    public Asset(String label, Identifier primary, Identifier[] secondaries, Device[] devices, Asset... children) {
        super(label, primary, secondaries);
        this.children = children;
        this.devices = devices;
    }

    public Device[] getDevices() {
        return devices;
    }

    public Asset[] getChildren() {
        return children;
    }
}
