package org.openremote.beta.shared.inventory;

import org.apache.camel.EndpointConfiguration;

public class Device extends InventoryObject {

    public enum Status {
        UNINITIALIZED,
        INITIALIZING,
        ONLINE,
        OFFLINE,
        REMOVING,
        REMOVED,
        INITIALIZATION_ERROR,
        COMMUNICATION_ERROR,
        MAINTENANCE
    }

    protected Status status = Status.UNINITIALIZED;

    protected Device parent;

    protected EndpointConfiguration[] endpointConfigurations;

    protected Device() {
    }

    public Device(String label, Identifier primary, EndpointConfiguration... endpointConfigurations) {
        this(label, primary, new Identifier[0], null, endpointConfigurations);
    }


    public Device(String label, Identifier primary, Device parent, EndpointConfiguration... endpointConfigurations) {
        this(label, primary, new Identifier[0], parent, endpointConfigurations);
    }

    public Device(String label, Identifier primary, Identifier[] secondaries, EndpointConfiguration... endpointConfigurations) {
        this(label, primary, secondaries, null, endpointConfigurations);
    }

    public Device(String label, Identifier primary, Identifier[] secondaries, Device parent, EndpointConfiguration... endpointConfigurations) {
        super(label, primary, secondaries);
        this.parent = parent;
        this.endpointConfigurations = endpointConfigurations;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Device getParent() {
        return parent;
    }

    public void setParent(Device parent) {
        this.parent = parent;
    }

    public EndpointConfiguration[] getEndpointConfigurations() {
        return endpointConfigurations;
    }
}
