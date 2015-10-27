package org.openremote.server.persistence.flow;

import org.openremote.server.persistence.GenericDAOImpl;
import org.openremote.shared.flow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FlowDAOImpl extends GenericDAOImpl<Flow, String>
    implements FlowDAO {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDAOImpl.class);


    public FlowDAOImpl(EntityManager em) {
        super(em, Flow.class);
    }


    @Override
    public Flow makePersistent(Flow instance, boolean attemptMerge) {
        Flow flow = super.makePersistent(instance, attemptMerge);

        // Cascade persistence to nodes, slots, and wires of flow
        for (int i = 0; i < instance.nodes.length; i++) {
            Node node = instance.nodes[i];
            node.flow = flow;
            if (attemptMerge) {
                instance.nodes[i] = em.merge(node);
            } else {
                em.persist(node);
                instance.nodes[i] = node;
            }

            for (int j = 0; j < node.slots.length; j++) {
                Slot slot = node.slots[j];
                slot.node = instance.nodes[i];
                if (attemptMerge) {
                    node.slots[j] = em.merge(slot);
                } else {
                    em.persist(slot);
                    node.slots[j] = slot;
                }
            }
        }
        flow.nodes = instance.nodes;

        for (int i = 0; i < instance.wires.length; i++) {
            Wire wire = instance.wires[i];
            wire.flowId = flow.getId();
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
            Set<Node> nodes = new LinkedHashSet<>();
            List<Object[]> nodesResult = em.createQuery(nodeCriteria).getResultList();
            for (Object[] tuple : nodesResult) {
                Node node = (Node) tuple[0];
                node.addSlots((Slot) tuple[1]);
                nodes.remove(node);
                nodes.add(node);
            }
            flow.nodes = nodes.toArray(new Node[nodes.size()]);

            CriteriaQuery<Wire> wireCriteria = cb.createQuery(Wire.class);
            Root<Wire> wireRoot = wireCriteria.from(Wire.class);
            wireCriteria.where(cb.equal(
                wireRoot.get(Wire_.flowId), flow.getId()
            ));
            List<Wire> wiresResult = em.createQuery(wireCriteria).getResultList();
            flow.wires = wiresResult.toArray(new Wire[wiresResult.size()]);
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
}