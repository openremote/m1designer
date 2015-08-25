package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Flow extends FlowObject {

    private static final Logger LOG = LoggerFactory.getLogger(Flow.class);


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

    public Node[] findClientWidgetNodes() {
        Set<Node> collection = new HashSet<>();
        for (Node node : getNodes()) {
            if (node.isClientWidget())
                collection.add(node);
        }
        return collection.toArray(new Node[collection.size()]);
    }

    public void addNode(Node node) {
        Set<Node> collection = new HashSet<>(Arrays.asList(getNodes()));
        collection.add(node);
        this.nodes = collection.toArray(new Node[collection.size()]);
    }

    public Wire[] removeNode(Node node) {
        Set<Node> collection = new HashSet<>(Arrays.asList(getNodes()));
        collection.remove(node);
        this.nodes = collection.toArray(new Node[collection.size()]);
        return removeWiresOf(node);
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

    public boolean hasDependency(String flowId) {
        for (Flow dependency : dependencies) {
            if (dependency.getId().equals(flowId))
                return true;
            boolean hasDependency = dependency.hasDependency(flowId);
            if (hasDependency)
                return true;
        }
        return false;
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

    public Wire addWireBetweenSlots(Slot sourceSlot, Slot sinkSlot) {
        return addWire(new Wire(sourceSlot.getId(), sinkSlot.getId()));
    }

    public Wire addWire(Wire wire) {
        for (Wire existing : getWires()) {
            if (existing.equals(wire))
                return null;
        }
        List<Wire> list = new ArrayList<>(Arrays.asList(getWires()));
        list.add(wire);
        this.wires = list.toArray(new Wire[list.size()]);
        return wire;
    }

    public Wire removeWire(Wire wire) {
        Wire removed = null;
        ArrayList<Wire> collection = new ArrayList<>(Arrays.asList(getWires()));
        Iterator<Wire> it = collection.iterator();
        while (it.hasNext()) {
            if (it.next().equals(wire)) {
                removed = wire;
                it.remove();
            }
        }
        this.wires = collection.toArray(new Wire[collection.size()]);
        return removed;
    }

    public Wire removeWireBetweenSlots(Slot sourceSlot, Slot sinkSlot) {
        Wire removed = null;
        ArrayList<Wire> collection = new ArrayList<>(Arrays.asList(getWires()));
        Iterator<Wire> it = collection.iterator();
        while (it.hasNext()) {
            Wire wire = it.next();
            if (wire.getSourceId().equals(sourceSlot.getId())
                && wire.getSinkId().equals(sinkSlot.getId())) {
                it.remove();
                removed = wire;
            }
        }
        this.wires = collection.toArray(new Wire[collection.size()]);
        return removed;
    }

    public Wire[] removeWiresOf(Node node) {
        Set<Wire> collection = new HashSet<>();
        for (Slot slot : node.getSlots()) {
            if (!hasWires(slot.getId()))
                continue;
            if (slot.isOfType(Slot.TYPE_SINK)) {
                Wire[] wires = findWiresForSink(slot.getId());
                for (Wire wire : wires) {
                    collection.add(removeWire(wire));
                }
            } else if (slot.isOfType(Slot.TYPE_SOURCE)) {
                Wire[] wires = findWiresForSource(slot.getId());
                for (Wire wire : wires) {
                    collection.add(removeWire(wire));
                }
            }
        }
        return collection.toArray(new Wire[collection.size()]);
    }

    public Slot findSlotInAllFlows(String slotId) {
        Flow ownerFlow = findOwnerFlowOfSlot(slotId);
        if (ownerFlow != null)
            return ownerFlow.findSlot(slotId);
        return null;
    }

    public Slot findSlot(String slotId) {
        for (Node node : getNodes()) {
            Slot slot = node.findSlot(slotId);
            if (slot != null)
                return slot;
        }
        return null;
    }

    public Slot[] findSlotsWithPeer() {
        Set<Slot> collection = new HashSet<>();
        for (Node node : getNodes()) {
            for (Slot slot : node.getSlots()) {
                if (slot.getPeerIdentifier() != null)
                    collection.add(slot);
            }
        }
        return collection.toArray(new Slot[collection.size()]);
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

    public Node[] findWiredNodesOf(Node node) {
        Set<Node> collection = new HashSet<>();
        for (Slot source : node.findSlots(Slot.TYPE_SOURCE)) {
            Wire[] wires = findWiresForSource(source.getId());
            for (Wire wire : wires) {
                collection.add(findOwnerNode(wire.getSinkId()));
            }
        }
        for (Slot sink : node.findSlots(Slot.TYPE_SINK)) {
            Wire[] wires = findWiresForSink(sink.getId());
            for (Wire wire : wires) {
                collection.add(findOwnerNode(wire.getSourceId()));
            }
        }
        return collection.toArray(new Node[collection.size()]);
    }

    public Wire[] findWiresBetween(Node a, Node b) {
        Set<Wire> collection = new HashSet<>();
        for (Wire wire : getWires()) {
            Node sourceOwner = findOwnerNode(wire.getSourceId());
            Node sinkOwner = findOwnerNode(wire.getSinkId());
            if ((sourceOwner.getId().equals(a.getId()) && sinkOwner.getId().equals(b.getId()))
                || (sourceOwner.getId().equals(b.getId()) && sinkOwner.getId().equals(a.getId()))) {
                collection.add(wire);
            }
        }
        return collection.toArray(new Wire[collection.size()]);
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
