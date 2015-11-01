package org.openremote.server.persistence.flow;

import org.openremote.server.persistence.GenericDAOImpl;
import org.openremote.shared.flow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;

public class FlowDAOImpl extends GenericDAOImpl<Flow, String>
    implements FlowDAO {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDAOImpl.class);

    public FlowDAOImpl(EntityManager em) {
        super(em, Flow.class);
    }

    @Override
    public List<Flow> findAll() {
        CriteriaQuery<Flow> c = em.getCriteriaBuilder().createQuery(Flow.class);
        Root<Flow> flowRoot = c.from(Flow.class);
        c.select(flowRoot).orderBy(em.getCriteriaBuilder().desc(flowRoot.get(Flow_.createdOn)));
        return em.createQuery(c).getResultList();
    }

    @Override
    public Flow makePersistent(Flow instance, boolean attemptMerge) {
        LOG.debug("Making flow persistent (attempt merge: " + attemptMerge + "): " + instance);
        Flow flow = super.makePersistent(instance, attemptMerge);

        // The following procedures are implementing what Hibernate would do when merging collections. We don't
        // have persistent collection mappings (need arrays for JS transpiler), so has to be done manually.

        // Persist nodes of flow
        Map<Node, Set<Slot>> oldNodes = findNodesAndSlots(flow);
        LOG.debug("Persisting nodes of flow: " + instance.nodes.length);
        for (int i = 0; i < instance.nodes.length; i++) {
            Node node = instance.nodes[i];
            node.flow = flow;
            LOG.debug("Persisting node: " + node);
            if (attemptMerge) {
                instance.nodes[i] = em.merge(node);
            } else {
                em.persist(node);
                instance.nodes[i] = node;
            }

            LOG.debug("Node has slots: " + node.slots.length);
            for (int j = 0; j < node.slots.length; j++) {
                Slot slot = node.slots[j];
                slot.node = instance.nodes[i];
                LOG.debug("Persisting slot: " + slot);
                if (attemptMerge) {
                    node.slots[j] = em.merge(slot);
                } else {
                    em.persist(slot);
                    node.slots[j] = slot;
                }
            }

            // Remove slots of node we no longer have
            Set<Slot> oldSlots = oldNodes.get(node);
            if (oldSlots != null) {
                LOG.debug("Checking existing slots for obsoletes: " + oldSlots.size());
                for (Slot oldSlot : oldSlots) {
                    if (node.findSlot(oldSlot.getId()) == null) {
                        LOG.debug("Removing obsolete slot: " + oldSlot);
                        em.remove(oldSlot);
                    }
                }
            }
        }
        flow.nodes = instance.nodes;

        // Remove nodes of the flow we no longer have
        LOG.debug("Checking existing nodes for obsoletes: " + oldNodes.size());
        for (Node oldNode: oldNodes.keySet()) {
            if (flow.findNode(oldNode.getId()) == null) {
                LOG.debug("Removing obsolete node: " + oldNode);
                em.remove(oldNode);
            }
        }

        for (int i = 0; i < instance.wires.length; i++) {
            Wire wire = instance.wires[i];
            wire.flowId = flow.getId();
            LOG.debug("Persisting wire: " + wire);
            if (attemptMerge) {
                instance.wires[i] = em.merge(wire);
            } else {
                em.persist(wire);
                instance.wires[i] = wire;
            }
        }
        flow.wires = instance.wires;

        return flow;
    }

    @Override
    public Flow findById(String id, boolean populateNodesAndWires) {
        Flow flow = super.findById(id);
        if (flow == null)
            return null;

        if (populateNodesAndWires) {
            flow.nodes = findNodes(flow);
            flow.wires = findWires(flow);
        }

        return flow;
    }

    @Override
    public Flow[] findSubflowDependents(String flowId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Flow> flowCriteria = cb.createQuery(Flow.class);
        Root<Flow> flowRoot = flowCriteria.from(Flow.class);
        Root<Node> nodeRoot = flowCriteria.from(Node.class);
        flowCriteria.where(cb.and(
            cb.equal(
                nodeRoot.get(Node_.flow), flowRoot
            ),
            cb.equal(
                nodeRoot.get(Node_.type), Node.TYPE_SUBFLOW
            ),
            cb.equal(
                nodeRoot.get(Node_.subflowId), flowId
            )
        ));
        flowCriteria.select(flowRoot).distinct(true);
        List<Flow> result = em.createQuery(flowCriteria).getResultList();

        // Must populate all
        // TODO: Join fetch instead?
        for (int i = 0; i < result.size(); i++) {
            Flow flow = result.get(i);
            result.set(i, findById(flow.getId(), true));
        }
        return result.toArray(new Flow[result.size()]);
    }

    protected Node[] findNodes(Flow flow) {
        Map<Node, Set<Slot>> nodesAndSlots = findNodesAndSlots(flow);
        Node[] nodes = new Node[nodesAndSlots.size()];
        Iterator<Map.Entry<Node, Set<Slot>>> it = nodesAndSlots.entrySet().iterator();
        for (int i = 0; i < nodes.length; i++) {
            Map.Entry<Node, Set<Slot>> entry = it.next();
            nodes[i] = entry.getKey();
            nodes[i].setSlots(entry.getValue().toArray(new Slot[entry.getValue().size()]));
        }
        return nodes;
    }

    protected Map<Node, Set<Slot>> findNodesAndSlots(Flow flow) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Object[]> nodeCriteria = cb.createQuery(Object[].class);
        Root<Node> nodeRoot = nodeCriteria.from(Node.class);
        Root<Slot> slotRoot = nodeCriteria.from(Slot.class);
        nodeCriteria.where(cb.and(
            cb.equal(
                slotRoot.get(Slot_.node), nodeRoot
            ),
            cb.equal(
                nodeRoot.get(Node_.flow), flow
            )
        ));
        nodeCriteria.multiselect(nodeRoot, slotRoot);
        List<Object[]> nodesResult = em.createQuery(nodeCriteria).getResultList();

        // This is all just result transformation
        Map<Node, Set<Slot>> nodesAndSlots = new LinkedHashMap<>();
        for (Object[] tuple : nodesResult) {
            Node node = (Node) tuple[0];
            Slot slot = (Slot) tuple[1];

            if (!nodesAndSlots.containsKey(node)) {
                nodesAndSlots.put(node, new LinkedHashSet<>());
            }
            nodesAndSlots.get(node).add(slot);
        }
        return nodesAndSlots;
    }

    protected Wire[] findWires(Flow flow) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Wire> wireCriteria = cb.createQuery(Wire.class);
        Root<Wire> wireRoot = wireCriteria.from(Wire.class);
        wireCriteria.where(cb.equal(
            wireRoot.get(Wire_.flowId), flow.getId()
        ));
        List<Wire> wiresResult = em.createQuery(wireCriteria).getResultList();
        return wiresResult.toArray(new Wire[wiresResult.size()]);
    }
}