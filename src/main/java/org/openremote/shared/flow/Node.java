package org.openremote.shared.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.model.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsType
public class Node extends FlowObject {

    public static final String TYPE_SUBFLOW = "urn:openremote:flow:node:subflow";
    public static final String TYPE_SUBFLOW_LABEL = "Subflow";

    public static final String TYPE_CONSUMER = "urn:openremote:flow:node:consumer";
    public static final String TYPE_CONSUMER_LABEL = "Sink";

    public static final String TYPE_PRODUCER = "urn:openremote:flow:node:producer";
    public static final String TYPE_PRODUCER_LABEL = "Source";

    public String subflowId;
    public Slot[] slots = new Slot[0];
    public boolean clientAccess;
    public boolean clientWidget;
    public String preEndpoint;
    public String postEndpoint;
    public EditorSettings editorSettings = new EditorSettings();
    public String properties;
    public String[] persistentPropertyPaths;

    protected Node() {
    }

    public Node(String label, Identifier identifier) {
        super(label, identifier);
    }

    public Node(String label, Identifier identifier, String subflowId) {
        super(label, identifier);
        if (!identifier.getType().equals(TYPE_SUBFLOW)) {
            throw new IllegalArgumentException(
                "Node with subflow identifier must be of type: " + TYPE_SUBFLOW
            );
        }
        this.subflowId = subflowId;
    }

    public boolean isOfTypeSubflow() {
        return isOfType(TYPE_SUBFLOW);
    }

    public boolean isOfTypeConsumerOrProducer() {
        return isOfType(TYPE_CONSUMER) || isOfType(TYPE_PRODUCER);
    }

    public String getSubflowId() {
        return subflowId;
    }

    public void setSubflowId(String subflowId) {
        this.subflowId = subflowId;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public void setSlots(Slot[] slots) {
        this.slots = slots;
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

    public String[] getPersistentPropertyPaths() {
        return persistentPropertyPaths;
    }

    public void setPersistentPropertyPaths(String[] persistentPropertyPaths) {
        this.persistentPropertyPaths = persistentPropertyPaths;
    }

    public void addSlots(Slot[] newSlots) {
        List<Slot> list = new ArrayList<>();
        list.addAll(Arrays.asList(getSlots()));
        list.addAll(Arrays.asList(newSlots));
        setSlots(list.toArray(new Slot[list.size()]));
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

    public Slot findSlotWithPeer(String peerId) {
        for (Slot slot : getSlots()) {
            if (slot.getPeerId() != null && slot.getPeerId().equals(peerId))
                return slot;
        }
        return null;
    }

    public Slot[] findAllConnectableSlots() {
        return findConnectableSlots(null);
    }

    public Slot[] findConnectableSlots(String type) {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if ((type == null || slot.isOfType(type)) && slot.isConnectable())
                list.add(slot);
        }
        return list.toArray(new Slot[list.size()]);
    }

    public Slot[] findNonPropertySlots(String type) {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if (slot.isOfType(type) && slot.getPropertyPath() == null)
                list.add(slot);
        }
        return list.toArray(new Slot[list.size()]);
    }

    public Slot[] findPropertySlots() {
        List<Slot> list = new ArrayList<>();
        for (Slot slot : getSlots()) {
            if (slot.getPropertyPath() != null)
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
