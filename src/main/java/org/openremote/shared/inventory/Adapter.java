package org.openremote.shared.inventory;

import org.openremote.shared.model.Identifier;

public class Adapter extends InventoryObject {

    protected String discoveryEndpoint;

    protected Adapter() {
    }

    public Adapter(String label, Identifier id, Identifier... keys) {
        super(label, id, keys);
    }

    public Adapter(String label, Identifier id, String discoveryEndpoint) {
        super(label, id);
        this.label = label;
        this.discoveryEndpoint = discoveryEndpoint;
    }

    public String getDiscoveryEndpoint() {
        return discoveryEndpoint;
    }

    public void setDiscoveryEndpoint(String discoveryEndpoint) {
        this.discoveryEndpoint = discoveryEndpoint;
    }

    @Override
    public String toString() {
        return "Adapter{" +
            "discoveryEndpoint='" + discoveryEndpoint + '\'' +
            "} " + super.toString();
    }
}
