package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

import static org.openremote.beta.shared.flow.Slot.Type.SINK;

public class Node {

    public String id;
    public String type;
    public String label;
    public Slot[] slots = new Slot[0];
    public Object properties;

    public Node() {
    }

    public Node(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public Node(String id, String type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }

    public Node(String id, String type, String label, Slot... slots) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.slots = slots;
    }

    public Node(String id, String type, String label, Slot[] slots, Object properties) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.slots = slots;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public Object getProperties() {
        return properties;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

}
