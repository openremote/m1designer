package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsType
public class Flow extends FlowObject {

    private static final Logger LOG = LoggerFactory.getLogger(Flow.class);

    public static final String TYPE = "urn:org-openremote:flow";

    public Node[] nodes = new Node[0];
    public Wire[] wires = new Wire[0];
    public FlowDependency[] superDependencies = new FlowDependency[0];
    public FlowDependency[] subDependencies = new FlowDependency[0];

    protected Flow() {
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

    public void clearDependencies() {
        setSuperDependencies(new FlowDependency[0]);
        setSubDependencies(new FlowDependency[0]);
    }

    public FlowDependency[] getSuperDependencies() {
        return superDependencies;
    }

    public void setSuperDependencies(FlowDependency[] superDependencies) {
        this.superDependencies = superDependencies;
    }

    public FlowDependency[] getSubDependencies() {
        return subDependencies;
    }

    public void setSubDependencies(FlowDependency[] subDependencies) {
        this.subDependencies = subDependencies;
    }

    public FlowDependency findSubDependency(Node subflowNode) {
        for (FlowDependency subDependency : getSubDependencies()) {
            if (subDependency.getId().equals(subflowNode.getSubflowId()))
                return subDependency;
        }
        return null;
    }

    public boolean hasWiredSuperDependency() {
        return getSuperDependencies() != null
            && getSuperDependencies().length > 0
            && getSuperDependencies()[0].isWired();
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

    public Node[] findSubflowNodes() {
        Set<Node> collection = new HashSet<>();
        for (Node node : getNodes()) {
            if (node.isOfType(Node.TYPE_SUBFLOW))
                collection.add(node);
        }
        return collection.toArray(new Node[collection.size()]);
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
