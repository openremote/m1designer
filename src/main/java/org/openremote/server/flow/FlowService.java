package org.openremote.server.flow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.inventory.InventoryService;
import org.openremote.server.persistence.PersistenceService;
import org.openremote.server.persistence.flow.DAOFlowDependencyResolver;
import org.openremote.server.persistence.flow.FlowDAO;
import org.openremote.server.route.RouteManagementService;
import org.openremote.server.route.procedure.FlowProcedureException;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowDependencyResolver;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.openremote.server.util.JsonUtil.JSON;

public class FlowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowService.class);

    protected class FlowDependencyResolverImpl extends DAOFlowDependencyResolver {

        public FlowDependencyResolverImpl(PersistenceService ps, EntityManager em) {
            super(ps, em);
        }

        @Override
        protected void stopFlowIfRunning(Flow flow) {
            if (FlowService.this.routeManagementService.isRunning(flow)) {
                LOG.debug("Stopping super flow after updating its subflow nodes");
                try {
                    routeManagementService.stopFlowRoutes(flow);
                } catch (FlowProcedureException ex) {
                    LOG.error("TODO: Error stopping flow, we must continue though...");
                }
            }
        }
    }

    ;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public static Flow getCopy(Flow flow) {
        try {
            return flow != null ? JSON.readValue(JSON.writeValueAsString(flow), Flow.class) : null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    final protected CamelContext context;
    final protected RouteManagementService routeManagementService;
    final protected InventoryService inventoryService;

    public FlowService(CamelContext context, RouteManagementService routeManagementService, InventoryService inventoryService) {
        this.context = context;
        this.routeManagementService = routeManagementService;
        this.inventoryService = inventoryService;
    }

    public Flow[] getFlows() {
        LOG.debug("Getting flows");
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
            List<Flow> flows = flowDAO.findAll();
            return flows.toArray(new Flow[flows.size()]);
        });
    }

    public Flow getFlowTemplate() {
        LOG.debug("Getting flow template");
        return new Flow("My Flow", IdentifierUtil.generateGlobalUniqueId());
    }

    public Node createSubflowNode(@Header("id") String id) {
        LOG.debug("Creating subflow node of: " + id);
        Flow flow = getFlow(id, false);
        if (flow == null)
            return null;

        NodeDescriptor subflowDescriptor = context.getRegistry().lookupByNameAndType(Node.TYPE_SUBFLOW, NodeDescriptor.class);
        if (subflowDescriptor == null)
            return null;

        Node subflowNode = subflowDescriptor.createNode();
        subflowNode.setLabel(flow.getLabel());
        subflowNode.setSubflowId(flow.getId());

        // Find peer slots in consumers/producers of target flow
        List<Slot> slots = new ArrayList<>();
        Node[] consumers = flow.findNodes(Node.TYPE_CONSUMER);
        for (Node consumer : consumers) {
            Slot firstSink = consumer.findSlots(Slot.TYPE_SINK)[0];
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSink, consumer.getLabel()));
        }
        Node[] producers = flow.findNodes(Node.TYPE_PRODUCER);
        for (Node producer : producers) {
            Slot firstSource = producer.findSlots(Slot.TYPE_SOURCE)[0];
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSource, producer.getLabel()));
        }
        subflowNode.addSlots(slots.toArray(new Slot[slots.size()]));

        return subflowNode;
    }

    public Flow getFlow(@Header("id") String id, @Header("hydrateSubs") Boolean hydrateSubs) {
        LOG.debug("Getting flow: " + id);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
            Flow flow = flowDAO.findById(id, true);
            if (flow == null)
                return null;
            new FlowDependencyResolverImpl(ps, em)
                .populateDependencies(flow, hydrateSubs != null ? hydrateSubs : false);
            return flow;
        });
    }

    public Flow getPresetFlow(@Header("agent") String agent,
                              @Header("width") Integer width,
                              @Header("height") Integer height) {
        ClientPresetVariant clientPresetVariant = new ClientPresetVariant(agent, width, height);
        LOG.debug("Getting preset flow: " + clientPresetVariant);
        String flowId = null;
        ClientPreset[] presets = inventoryService.getClientPresets();
        for (ClientPreset preset : presets) {
            if (preset.matches(clientPresetVariant) && preset.getInitialFlowId() != null) {
                LOG.debug("Matching preset for variant, using flow: " + preset.getInitialFlowId());
                flowId = preset.getInitialFlowId();
                break;
            }
        }
        return flowId != null ? getFlow(flowId, true) : null;
    }

    public void deleteFlow(String id) throws FlowProcedureException {
        LOG.debug("Delete flow: " + id);
        context.hasService(PersistenceService.class).transactional((ps, em) -> {
            FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
            Flow flow = flowDAO.findById(id, true);
            if (flow == null)
                return null;

            FlowDependencyResolver flowDependencyResolver = new FlowDependencyResolverImpl(ps, em);

            flowDependencyResolver.populateDependencies(flow, false);
            flowDependencyResolver.updateDependencies(flow, true);

            // TODO: Exception handling
            try {
                routeManagementService.stopFlowRoutes(flow);
            } catch (FlowProcedureException ex) {
                throw new RuntimeException(ex);
            }

            flowDAO.makeTransient(flow);

            return null;
        });
    }

    public void postFlow(Flow flow) {
        LOG.debug("Posting new flow: " + flow);
        context.hasService(PersistenceService.class).transactional((ps, em) -> {

            if (flow.getSuperDependencies().length > 0 || flow.getSubDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when posting a flow");

            new FlowDependencyResolverImpl(ps, em).updateDependencies(flow, false);

            // TODO: Another consistency check, e.g. all wires good

            FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
            flowDAO.makePersistent(flow, false);

            return null;
        });
    }

    public boolean putFlow(Flow flow) {
        LOG.debug("Putting flow: " + flow);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {

            FlowDAO flowDAO = ps.getDAO(em, FlowDAO.class);
            Flow existingFlow = flowDAO.findById(flow.getId(), true);

            if (existingFlow == null)
                return false;

            if (flow.getSuperDependencies().length > 0 || flow.getSubDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when posting a flow");

            new FlowDependencyResolverImpl(ps, em).updateDependencies(flow, false);

            // TODO: Another consistency check, e.g. all wires good

            // TODO cleaner solution
            filterNonPersistentProperties(flow);

            flowDAO.makePersistent(flow, true);

            return true;
        });
    }

    public Flow getResolvedFlow(Flow flow, boolean hydrateSubs) {
        LOG.debug("Resolving dependencies of flow: " + flow);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            new FlowDependencyResolverImpl(ps, em).populateDependencies(flow, hydrateSubs);
            return flow;
        });
    }

    public void resetCopy(Node node) {
        node.setId(IdentifierUtil.generateGlobalUniqueId());

        for (Slot slot : node.getSlots()) {
            slot.setId(IdentifierUtil.generateGlobalUniqueId());
        }

        if (node.getLabel() != null) {
            if (!node.getLabel().endsWith("(Copy)")) {
                node.setLabel(node.getLabel() + " (Copy)");
            }
        } else {
            node.setLabel("(Copy)");
        }
    }

    protected void filterNonPersistentProperties(Flow flow) {
        try {
            for (Node node : flow.getNodes()) {
                if (node.getProperties() != null && node.getProperties() != null) {
                    LOG.debug("Filtering non-persistent properties of: " + node);

                    // Don't believe what the node says about its persistent property paths,
                    // must ask the node descriptor what those properties are
                    NodeDescriptor nodeDescriptor = context.getRegistry().lookupByNameAndType(
                        node.getType(),
                        NodeDescriptor.class
                    );
                    List<String> persistentPaths = nodeDescriptor.getPersistentPropertyPaths();

                    // Find all non-persistent properties
                    List<String> nonPersistentPaths = new ArrayList<>();
                    ObjectNode propertiesNode = JSON.readValue(node.getProperties(), ObjectNode.class);
                    Iterator<String> it = propertiesNode.fieldNames();
                    while (it.hasNext()) {
                        String path = it.next();
                        if (!persistentPaths.contains(path))
                            nonPersistentPaths.add(path);
                    }

                    // Filter all non-persistent properties
                    for (String nonPersistentPath : nonPersistentPaths) {
                        LOG.debug("Removing non-persistent property: " + nonPersistentPath);
                        propertiesNode.remove(nonPersistentPath);
                    }
                    node.setProperties(JSON.writeValueAsString(propertiesNode));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
