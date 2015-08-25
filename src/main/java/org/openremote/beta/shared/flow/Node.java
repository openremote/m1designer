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
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Node extends FlowObject {

    public static final String TYPE_SUBFLOW = "urn:org-openremote:flow:node:subflow";
    public static final String TYPE_SUBFLOW_LABEL = "Flow";

    public Slot[] slots = new Slot[0];
    public boolean clientAccess;
    public boolean clientWidget;
    public String preEndpoint;
    public String postEndpoint;
    public EditorSettings editorSettings = new EditorSettings();
    public String properties;

    public Node() {
    }

    public Node(String label, Identifier identifier) {
        super(label, identifier);
    }

    public Node(String label, Identifier identifier, Slot... slots) {
        super(label, identifier);
        this.slots = slots;
    }

    public Node(String label, Identifier identifier, Slot[] slots, String properties) {
        super(label, identifier);
        this.slots = slots;
        this.properties = properties;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public boolean isClientAccess() {
        return clientAccess;
    }

    public void setClientAccess(boolean clientAccess) {
        this.clientAccess = clientAccess;
    }

    public boolean isClientWidget() {
        return clientWidget;
    }

    public void setClientWidget(boolean clientWidget) {
        this.clientWidget = clientWidget;
    }

    public String getPreEndpoint() {
        return preEndpoint;
    }

    public void setPreEndpoint(String preEndpoint) {
        this.preEndpoint = preEndpoint;
    }

    public String getPostEndpoint() {
        return postEndpoint;
    }

    public void setPostEndpoint(String postEndpoint) {
        this.postEndpoint = postEndpoint;
    }

    public EditorSettings getEditorSettings() {
        return editorSettings;
    }

    public void setEditorSettings(EditorSettings editorSettings) {
        this.editorSettings = editorSettings;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
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

    public Slot[] findConnectableSlots(String type) {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if (slot.isOfType(type) && slot.isConnectable())
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
