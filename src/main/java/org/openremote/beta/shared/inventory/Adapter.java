package org.openremote.beta.shared.inventory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
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
