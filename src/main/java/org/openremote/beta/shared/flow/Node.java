package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Node extends FlowObject {

    public static final String TYPE_CONSUMER = "urn:org-openremote:flow:node:consumer";
    public static final String TYPE_PRODUCER = "urn:org-openremote:flow:node:producer";
    public static final String TYPE_FUNCTION = "urn:org-openremote:flow:node:function";
    public static final String TYPE_CHANGE = "urn:org-openremote:flow:node:change";
    public static final String TYPE_STORAGE = "urn:org-openremote:flow:node:storage";

    public static String getTypeLabel(String type) {
        switch(type) {
            case TYPE_CONSUMER:
                return "Consumer";
            case TYPE_PRODUCER:
                return "Producer";
            case TYPE_FUNCTION:
                return "Function";
            case TYPE_CHANGE:
                return "Change";
            case TYPE_STORAGE:
                return "Storage";
            default:
                return type;
        }
    }

    public Slot[] slots = new Slot[0];
    public Object properties;

    public Node() {
    }

    public Node(String label, Identifier identifier) {
        super(label, identifier);
    }

    public Node(String label, Identifier identifier, Slot... slots) {
        super(label, identifier);
        this.slots = slots;
    }

    public Node(String label, Identifier identifier, Slot[] slots, Object properties) {
        super(label, identifier);
        this.slots = slots;
        this.properties = properties;
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
        for (Slot slot : getSlots()) {
            if (slot.getIdentifier().getId().equals(slotId))
                return slot;
        }
        return null;
    }

    public Slot[] findSlots(String type) {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if (slot.isOfType(type))
                list.add(slot);
        }
        return list.toArray(new Slot[list.size()]);
    }

    @Override
    public String toString() {
        return "Node{" +
            "properties=" + properties +
            ", slots=" + slots.length +
            "} " + super.toString();
    }
}
