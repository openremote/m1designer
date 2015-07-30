package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;

import java.util.ArrayList;
import java.util.List;

@JsType
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

    public boolean hasProperties() {
        return getProperties() != null;
    }

    public Slot findSlot(String slotId) {
        return findSlotByType(slotId, null);
    }

    public Slot findSlotByType(String slotId, Slot.Type type) {
        for (Slot slot : getSlots()) {
            if (slot.getId().equals(slotId) && (type == null || slot.getType() == type))
                return slot;
        }
        return null;
    }

    public Slot[] findSlots(Slot.Type type) {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if (slot.getType().equals(type))
                list.add(slot);
        }
        return list.toArray(new Slot[list.size()]);
    }

    @Override
    public String toString() {
        return "Node{" +
            "'" + label + '\'' +
            ", id='" + id + '\'' +
            ", type='" + type + '\'' +
            ", slots=" + slots.length +
            '}';
    }
}
