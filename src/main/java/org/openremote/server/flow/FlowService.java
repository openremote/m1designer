package org.openremote.server.flow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.inventory.InventoryService;
import org.openremote.server.route.RouteManagementService;
import org.openremote.server.route.procedure.FlowProcedureException;
import org.openremote.server.testdata.SampleEnvironmentWidget;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.*;
import org.openremote.shared.inventory.ClientPreset;
import org.openremote.shared.model.Identifier;
import org.openremote.shared.inventory.ClientPresetVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.openremote.server.util.JsonUtil.JSON;

public class FlowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowService.class);

    final static protected Map<String, Flow> SAMPLE_FLOWS = new LinkedHashMap<>();
    final protected FlowDependencyResolver SAMPLE_DEPENDENCY_RESOLVER = new MapFlowDependencyResolver(SAMPLE_FLOWS) {
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

        @Override
        protected String generateGlobalUniqueId() {
            return IdentifierUtil.generateGlobalUniqueId();
        }
    };

    public static abstract class MapFlowDependencyResolver extends FlowDependencyResolver {

        final Map<String, Flow> flows;

        public MapFlowDependencyResolver(Map<String, Flow> flows) {
            this.flows = flows;
        }

        @Override
        protected Flow findFlow(String flowId) {
            synchronized (flows) {
                return flows.get(flowId);
            }
        }

        @Override
        protected Flow[] findSubflowDependents(String flowId) {
            synchronized (flows) {
                List<Flow> list = new ArrayList<>();
                for (Flow flow : flows.values()) {
                    Node[] subflowNodes = flow.findSubflowNodes();
                    for (Node subflowNode : subflowNodes) {
                        if (flowId.equals(subflowNode.getSubflowId())) {
                            list.add(flow);
                            break;
                        }
                    }
                }
                return list.toArray(new Flow[list.size()]);
            }
        }

        @Override
        protected void storeFlow(Flow flow) {
            synchronized (flows) {
                flows.put(flow.getId(), flow);
            }
        }
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

    @Override
    public void start() throws Exception {
        synchronized (SAMPLE_FLOWS) {
            // TODO sample data
            SAMPLE_FLOWS.put(SampleEnvironmentWidget.FLOW.getId(), SampleEnvironmentWidget.getCopy());
            SAMPLE_FLOWS.put(SampleThermostatControl.FLOW.getId(), SampleThermostatControl.getCopy());
            SAMPLE_FLOWS.put(SampleTemperatureProcessor.FLOW.getId(), SampleTemperatureProcessor.getCopy());
        }
    }

    @Override
    public void stop() throws Exception {
        synchronized (SAMPLE_FLOWS) {
            SAMPLE_FLOWS.clear();
        }
    }

    public Flow[] getFlows() {
        LOG.debug("Getting sample flows");
        synchronized (SAMPLE_FLOWS) {
            // Shallow copy, we only want the label and id of the flow
            Flow[] flowsInfo = new Flow[SAMPLE_FLOWS.size()];
            int i = 0;
            for (Flow flow : SAMPLE_FLOWS.values()) {
                flowsInfo[i++] = new Flow(flow.getLabel(), flow.getIdentifier());
            }
            return flowsInfo;
        }
    }

    public Flow getFlowTemplate() {
        LOG.debug("Getting flow template");
        return new Flow("My Flow", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Flow.TYPE));
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

        slots.addAll(Arrays.asList(subflowNode.getSlots()));

        subflowNode.setSlots(slots.toArray(new Slot[slots.size()]));

        return subflowNode;
    }

    public Flow getFlow(@Header("id") String id, @Header("hydrateSubs") Boolean hydrateSubs) {
        LOG.debug("Getting flow: " + id);
        synchronized (SAMPLE_FLOWS) {

            Flow flow = getCopy(SAMPLE_FLOWS.get(id));
            if (flow == null)
                return null;

            SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(flow, hydrateSubs != null ? hydrateSubs : false);

            return flow;
        }
    }

    public Flow getPresetFlow(@Header("agent") String agent, @Header("width") Integer width, @Header("height") Integer height) {
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
        return getFlow(flowId, true);
    }

    public void deleteFlow(String id) throws Exception {
        LOG.debug("Delete flow: " + id);
        synchronized (SAMPLE_FLOWS) {

            Flow flow = getFlow(id, false);
            if (flow == null)
                return;

            SAMPLE_DEPENDENCY_RESOLVER.updateDependencies(flow, true);

            // TODO: Exception handling
            routeManagementService.stopFlowRoutes(flow);

            SAMPLE_FLOWS.remove(id);
        }
    }

    public void postFlow(Flow flow) {
        LOG.debug("Posting new flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            if (flow.getSuperDependencies().length > 0 || flow.getSubDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when posting a flow");

            SAMPLE_DEPENDENCY_RESOLVER.updateDependencies(flow, false);

            // TODO: Another consistency check, e.g. all wires good
            SAMPLE_FLOWS.put(flow.getId(), flow);
        }
    }

    public boolean putFlow(Flow flow) {
        LOG.debug("Putting flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            if (!SAMPLE_FLOWS.containsKey(flow.getId()))
                return false;

            if (flow.getSuperDependencies().length > 0 || flow.getSubDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when updating a flow");

            SAMPLE_DEPENDENCY_RESOLVER.updateDependencies(flow, false);

            // TODO cleaner solution
            filterNonPersistentProperties(flow);

            // TODO: Another consistency check, e.g. all wires good
            SAMPLE_FLOWS.put(flow.getId(), flow);

            return true;
        }
    }

    public Flow getResolvedFlow(Flow flow, boolean hydrateSubs) {
        LOG.debug("Resolving dependencies of flow: " + flow);
        synchronized (SAMPLE_FLOWS) {
            SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(flow, hydrateSubs);
            return flow;
        }
    }

    public void resetCopy(Node node) {
        node.getIdentifier().setId(IdentifierUtil.generateGlobalUniqueId());

        for (Slot slot : node.getSlots()) {
            slot.getIdentifier().setId(IdentifierUtil.generateGlobalUniqueId());
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

                    // Don't believe what the node says about its persistent property paths,
                    // must ask the node descriptor what those properties are
                    NodeDescriptor nodeDescriptor = context.getRegistry().lookupByNameAndType(
                        node.getIdentifier().getType(),
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
