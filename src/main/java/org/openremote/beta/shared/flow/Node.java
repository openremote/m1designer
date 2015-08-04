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
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Node extends FlowObject {

    // TODO We need a node type system

    public static final String TYPE_CONSUMER = "urn:org-openremote:flow:node:consumer";
    public static final String TYPE_PRODUCER = "urn:org-openremote:flow:node:producer";
    public static final String TYPE_SENSOR = "urn:org-openremote:flow:node:sensor";
    public static final String TYPE_ACTUATOR = "urn:org-openremote:flow:node:actuator";
    public static final String TYPE_FUNCTION = "urn:org-openremote:flow:node:function";
    public static final String TYPE_FILTER = "urn:org-openremote:flow:node:filter";
    public static final String TYPE_CHANGE = "urn:org-openremote:flow:node:change";
    public static final String TYPE_STORAGE = "urn:org-openremote:flow:node:storage";
    public static final String TYPE_SUBFLOW = "urn:org-openremote:flow:node:subflow";
    public static final String TYPE_WIDGET = "urn:org-openremote:flow:node:widget";

    public static String getTypeLabel(String type) {
        switch (type) {
            case TYPE_CONSUMER:
                return "Consumer";
            case TYPE_PRODUCER:
                return "Producer";
            case TYPE_SENSOR:
                return "Sensor";
            case TYPE_ACTUATOR:
                return "Actuator";
            case TYPE_FUNCTION:
                return "Function";
            case TYPE_FILTER:
                return "Filter";
            case TYPE_CHANGE:
                return "Change";
            case TYPE_STORAGE:
                return "Storage";
            case TYPE_SUBFLOW:
                return "Flow";
            case TYPE_WIDGET:
                return "Widget";
            default:
                return type;
        }
    }

    static String SENSOR_COLOR = "blue";
    static String ACTUATOR_COLOR = "darkblue";
    static String VIRTUAL_COLOR = "limegreen";
    static String PROCESSOR_COLOR = "sandybrown";
    static String UI_WIDGET_COLOR = "deeppink";

    public static String getNodeColor(Node node) {
        switch (node.getIdentifier().getType()) {
            case TYPE_CONSUMER:
                return VIRTUAL_COLOR;
            case TYPE_PRODUCER:
                return VIRTUAL_COLOR;
            case TYPE_SENSOR:
                return SENSOR_COLOR;
            case TYPE_ACTUATOR:
                return ACTUATOR_COLOR;
            case TYPE_FUNCTION:
                return PROCESSOR_COLOR;
            case TYPE_FILTER:
                return PROCESSOR_COLOR;
            case TYPE_CHANGE:
                return PROCESSOR_COLOR;
            case TYPE_STORAGE:
                return PROCESSOR_COLOR;
            case TYPE_SUBFLOW:
                return VIRTUAL_COLOR;
            case TYPE_WIDGET:
                return UI_WIDGET_COLOR;
            default:
                return null;
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

    public Slot findSlotByPosition(int position, String type) {
        if (position > getSlots().length-1)
            return null;
        if (getSlots()[position].getIdentifier().getType().equals(type))
            return getSlots()[position];
        return null;
    }
}
