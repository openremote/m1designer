package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@JsType
public class Flow {

    public String id;
    public String label;
    public Node[] nodes = new Node[0];
    public Wire[] wires = new Wire[0];

    public Flow() {
    }

    public Flow(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public Flow(String id, String label, Node... nodes) {
        this.id = id;
        this.label = label;
        this.nodes = nodes;
    }

    public Flow(String id, String label, Node[] nodes, Wire[] wires) {
        this.id = id;
        this.label = label;
        this.nodes = nodes;
        this.wires = wires;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        Set<Node> collection = new HashSet<>(Arrays.asList(getNodes()));
        collection.add(node);
        this.nodes = collection.toArray(new Node[collection.size()]);
    }

    public void removeNode(Node node) {
        Set<Node> collection = new HashSet<>(Arrays.asList(getNodes()));
        collection.remove(node);
        this.nodes = collection.toArray(new Node[collection.size()]);
    }

    public Wire[] getWires() {
        return wires;
    }

    public void addWireBetweenSlots(Slot sourceSlot, Slot sinkSlot) {
        addWire(new Wire(sourceSlot.getId(), sinkSlot.getId()));
    }

    public void addWire(Wire wire) {
        Set<Wire> collection = new HashSet<>(Arrays.asList(getWires()));
        collection.add(wire);
        this.wires = collection.toArray(new Wire[collection.size()]);
    }

    public void removeWire(Slot sourceSlot, Slot sinkSlot) {
        Set<Wire> collection = new HashSet<>(Arrays.asList(getWires()));
        Iterator<Wire> it = collection.iterator();
        while (it.hasNext()) {
            Wire wire = it.next();
            if (wire.getSourceId().equals(sourceSlot.getId())
                && wire.getSinkId().equals(sinkSlot.getId())) {
                it.remove();
            }
        }
        this.wires = collection.toArray(new Wire[collection.size()]);
    }

    public Slot findSlot(String slotId) {
        for (Node node : getNodes()) {
            for (Slot slot : node.getSlots()) {
                if (slot.getId().equals(slotId))
                    return slot;
            }
        }
        return null;
    }

    public boolean hasWires(String slotId) {
        for (Wire wire : getWires()) {
            if (wire.getSourceId().equals(slotId))
                return true;
            if (wire.getSinkId().equals(slotId))
                return true;
        }
        return false;
    }

    public Node findOwnerNode(String slotId) {
        for (Node node : getNodes()) {
            for (Slot slot : node.getSlots()) {
                if (slot.getId().equals(slotId))
                    return node;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Flow{" +
            "id='" + id + '\'' +
            ", label='" + label + '\'' +
            '}';
    }
}
