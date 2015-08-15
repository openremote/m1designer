package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Flow extends FlowObject {

    public static final String TYPE = "urn:org-openremote:flow";

    public Node[] nodes = new Node[0];
    public Wire[] wires = new Wire[0];
    public Flow[] dependencies = new Flow[0];

    public Flow() {
    }

    public Flow(String label, Identifier identifier) {
        super(label, identifier);
    }

    public Flow(String label, Identifier identifier, Node... nodes) {
        super(label, identifier);
        this.nodes = nodes;
    }

    public Flow(String label, Identifier identifier, Node[] nodes, Wire[] wires) {
        super(label, identifier);
        this.nodes = nodes;
        this.wires = wires;
        if (wires != null) {
            // Consistency check, important to avoid duplicate wires
            Set<Wire> wireSet = new HashSet<>(Arrays.asList(wires));
            if (wireSet.size() != wires.length)
                throw new IllegalArgumentException("Duplicate wires: " + Arrays.toString(wires));
        }
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Node[] findNodes(String type) {
        List<Node> collection = new ArrayList<>(Arrays.asList(getNodes()));
        Iterator<Node> it = collection.iterator();
        while (it.hasNext()) {
            Node node = it.next();
            if (!node.getIdentifier().getType().equals(type))
                it.remove();
        }
        return collection.toArray(new Node[collection.size()]);
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

    public Flow[] getDependencies() {
        return dependencies;
    }

    public void addDependency(Flow flow) {
        Set<Flow> collection = new HashSet<>(Arrays.asList(getDependencies()));
        collection.add(flow);
        this.dependencies = collection.toArray(new Flow[collection.size()]);
    }

    public void clearDependencies() {
        this.dependencies = new Flow[0];
    }

    public Flow findOwnerFlowOfSlot(String slotId) {
        Slot slot = findSlot(slotId);
        if (slot != null)
            return this;

        for (Flow dependency : getDependencies()) {
            Flow result = dependency.findOwnerFlowOfSlot(slotId);
            if (result != null)
                return result;
        }

        return null;
    }

    public String[] findWiredSubflowSlotIds() {
        Set<String> collection = new HashSet<>();
        for (Wire wire : getWires()) {
            Node sourceNode = findOwnerNode(wire.getSourceId());
            if (sourceNode == null)
                throw new IllegalStateException(
                    "Dangling wire, no source node for slot '" + wire.getSourceId() + "' on: " + this
                );
            if (sourceNode.isOfType(Node.TYPE_SUBFLOW))
                collection.add(findSlot(wire.getSourceId()).getPeerIdentifier().getId());

            Node sinkNode = findOwnerNode(wire.getSinkId());
            if (sinkNode == null)
                throw new IllegalStateException(
                    "Dangling wire, no sink node for slot '" + wire.getSourceId() + "' on: " + this
                );
            if (sinkNode.isOfType(Node.TYPE_SUBFLOW))
                collection.add(findSlot(wire.getSinkId()).getPeerIdentifier().getId());
        }
        return collection.toArray(new String[collection.size()]);
    }

    public Flow findSubflow(Node subflowNode) {
        Slot[] slots = subflowNode.getSlots();
        // TODO: This assumes that the first slot we find will have the right peer
        for (Slot slot : slots) {
            String peerSlotId = slot.getPeerIdentifier().getId();
            if (peerSlotId == null)
                continue;
            Flow subflow = findOwnerFlowOfSlot(peerSlotId);
            if (subflow != null)
                return subflow;
        }
        return null;
    }

    public Wire[] getWires() {
        return wires;
    }

    public void addWireBetweenSlots(Slot sourceSlot, Slot sinkSlot) {
        addWire(new Wire(sourceSlot.getId(), sinkSlot.getId()));
    }

    public void addWire(Wire wire) {
        for (Wire existing : getWires()) {
            if (existing.equals(wire))
                return;
        }
        List<Wire> list = new ArrayList<>(Arrays.asList(getWires()));
        list.add(wire);
        this.wires = list.toArray(new Wire[list.size()]);
    }

    public void removeWire(Slot sourceSlot, Slot sinkSlot) {
        ArrayList<Wire> collection = new ArrayList<>(Arrays.asList(getWires()));
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
            Slot slot = node.findSlot(slotId);
            if (slot != null)
                return slot;
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

    public Wire[] findWiresForSource(String slotId) {
        List<Wire> list = new ArrayList<>();
        for (Wire wire : getWires()) {
            if (wire.getSourceId().equals(slotId))
                list.add(wire);
        }
        return list.toArray(new Wire[list.size()]);
    }

    public Wire[] findWiresForSink(String slotId) {
        List<Wire> list = new ArrayList<>();
        for (Wire wire : getWires()) {
            if (wire.getSinkId().equals(slotId))
                list.add(wire);
        }
        return list.toArray(new Wire[list.size()]);
    }

    public Node findNode(String nodeId) {
        for (Node node : getNodes()) {
            if (node.getId().equals(nodeId))
                return node;
        }
        return null;
    }

    public boolean isNodeWiredToNodeOfType(Node node, String nodeType) {
        for (Slot source : node.findSlots(Slot.TYPE_SOURCE)) {
            Wire[] wires = findWiresForSource(source.getId());
            for (Wire wire : wires) {
                Node otherSide = findOwnerNode(wire.getSinkId());
                if (!otherSide.getIdentifier().getType().equals(nodeType))
                    return true;
            }
        }
        for (Slot sink : node.findSlots(Slot.TYPE_SINK)) {
            Wire[] wires = findWiresForSink(sink.getId());
            for (Wire wire : wires) {
                Node otherSide = findOwnerNode(wire.getSourceId());
                if (!otherSide.getIdentifier().getType().equals(Node.TYPE_CLIENT))
                    return true;
            }
        }
        return false;
    }

    public Node findOwnerNode(String slotId) {
        for (Node node : getNodes()) {
            Slot slot = node.findSlot(slotId);
            if (slot != null)
                return node;
        }
        return null;
    }
}
