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
import static org.openremote.beta.shared.util.Util.*;

@JsType
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Node extends FlowObject {

    public static final String TYPE_SUBFLOW = "urn:org-openremote:flow:node:subflow";
    public static final String TYPE_SUBFLOW_LABEL = "Flow";

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

    public String getEditorPropertyString(String property) {
        if (!hasProperties())
            return null;
        return getString(getMap(getMap(getProperties()), "editor"), property);
    }

    public Double getEditorPropertyDouble(String property) {
        if (!hasProperties())
            return null;
        return getDouble(getMap(getMap(getProperties()), "editor"), property);
    }

    public boolean isClientAccessEnabled() {
        // Should we accept client message events for this node's sinks and should
        // we send client message events when this node received a message?
        return hasProperties() && getBoolean(getMap(getProperties()), "clientAccess");
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    public boolean hasProperties() {
        return getProperties() != null;
    }

    public Slot findSlot(String slotId) {
        for (Slot slot : getSlots()) {
            if (slot.getId().equals(slotId))
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

    public Slot findSlotByPosition(int position, String type) {
        if (position > getSlots().length-1)
            return null;
        if (getSlots()[position].getIdentifier().getType().equals(type))
            return getSlots()[position];
        return null;
    }
}
