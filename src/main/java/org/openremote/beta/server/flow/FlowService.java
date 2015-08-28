package org.openremote.beta.server.flow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.server.catalog.CatalogService;
import org.openremote.beta.server.catalog.NodeDescriptor;
import org.openremote.beta.server.route.ConsumerRoute;
import org.openremote.beta.server.route.ProducerRoute;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependencyResolver;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.openremote.beta.server.util.JsonUtil.JSON;

public class FlowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowService.class);

    final static protected Map<String, Flow> SAMPLE_FLOWS = new LinkedHashMap<>();
    static protected FlowDependencyResolver SAMPLE_DEPENDENCY_RESOLVER;

    public static Flow getCopy(Flow flow) {
        try {
            return flow != null ? JSON.readValue(JSON.writeValueAsString(flow), Flow.class) : null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    final protected CamelContext context;
    final protected RouteManagementService routeManagementService;

    public FlowService(CamelContext context, RouteManagementService routeManagementService) {
        this.context = context;
        this.routeManagementService = routeManagementService;
    }

    @Override
    public void start() throws Exception {
        synchronized (SAMPLE_FLOWS) {
            // TODO sample data
            SAMPLE_FLOWS.put(SampleEnvironmentWidget.FLOW.getId(), SampleEnvironmentWidget.getCopy());
            SAMPLE_FLOWS.put(SampleThermostatControl.FLOW.getId(), SampleThermostatControl.getCopy());
            SAMPLE_FLOWS.put(SampleTemperatureProcessor.FLOW.getId(), SampleTemperatureProcessor.getCopy());

            SAMPLE_DEPENDENCY_RESOLVER = new FlowDependencyResolver() {
                @Override
                protected Flow findFlow(String flowId) {
                    return SAMPLE_FLOWS.get(flowId);
                }
            };
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
        Flow flow = getFlow(id);
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
        Node[] consumers = flow.findNodes(ConsumerRoute.NODE_TYPE);
        for (Node consumer : consumers) {
            Slot firstSink = consumer.findSlots(Slot.TYPE_SINK)[0];
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSink, consumer.getLabel()));
        }
        Node[] producers = flow.findNodes(ProducerRoute.NODE_TYPE);
        for (Node producer : producers) {
            Slot firstSource = producer.findSlots(Slot.TYPE_SOURCE)[0];
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), firstSource, producer.getLabel()));
        }
        subflowNode.setSlots(slots.toArray(new Slot[slots.size()]));

        return subflowNode;
    }

    public Flow getFlow(@Header("id") String id) {
        LOG.debug("Getting flow: " + id);
        synchronized (SAMPLE_FLOWS) {

            Flow flow = getCopy(SAMPLE_FLOWS.get(id));
            if (flow == null)
                return null;

            // TODO Dependency resolution occurs when a flow is loaded?!
            // TODO Handle exceptions in resolution?
            try {
                SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(flow);
            } catch (IllegalStateException ex) {
                LOG.warn("Exception during flow resolution: " + ex.getMessage(), ex);
                return null;
            }

            return flow;
        }
    }

    public void deleteFlow(String id) throws Exception {
        LOG.debug("Delete flow: " + id);
        synchronized (SAMPLE_FLOWS) {
            Flow flow = getFlow(id);

            // TODO: We must prevent deletion of a flow that is used as a dependency in other flows, this is a hack
            for (String otherFlowId : SAMPLE_FLOWS.keySet()) {
                Flow otherFlow = getFlow(otherFlowId);
                if (otherFlow.hasDependency(id)) {
                    throw new IllegalStateException("Can't delete flow '" + id + "', is a dependency of : " + otherFlow);
                }
            }

            if (flow != null) {
                // TODO: Exception handling
                routeManagementService.stopFlowRoutes(flow);
                SAMPLE_FLOWS.remove(id);
            }
        }
    }

    public void postFlow(Flow flow) {
        LOG.debug("Posting new flow: " + flow);
        synchronized (SAMPLE_FLOWS) {
            SAMPLE_FLOWS.put(flow.getId(), flow);
        }
    }

    public Flow getResolvedFlow(Flow flow) {
        LOG.debug("Resolving dependencies of flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            // TODO Handle exceptions in resolution?
            SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(flow);
            LOG.info("### RESOLVED: " + Arrays.toString(flow.getDependencies()));
            return flow;
        }
    }

    public boolean putFlow(Flow flow) {
        LOG.debug("Putting flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            if (!SAMPLE_FLOWS.containsKey(flow.getId()))
                return false;

            if (flow.getDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when updating a flow");

            // TODO Find out if this flow has any subflow nodes in other flows, then update those dependents?
            // TODO And then do we redeploy the dependents automatically if they are running?
            // TODO If we don't redeploy the dependents, how will the user know this still needs to happen? Mark flows?
            updateSubflowDependencies(flow);

            // TODO cleaner solution
            filterNonPersistentProperties(flow);

            SAMPLE_FLOWS.put(flow.getId(), flow);

            return true;
        }
    }

    protected void filterNonPersistentProperties(Flow flow) {
        try {
            for (Node node : flow.getNodes()) {
                if (node.getProperties() != null && node.getProperties() != null) {

                    List<String> persistentPaths = node.getPersistentPropertyPaths() != null
                        ? Arrays.asList(node.getPersistentPropertyPaths())
                        : Collections.EMPTY_LIST;

                    List<String> nonPersistentPaths = new ArrayList<>();

                    ObjectNode propertiesNode = JSON.readValue(node.getProperties(), ObjectNode.class);

                    Iterator<String> it = propertiesNode.fieldNames();
                    while (it.hasNext()) {
                        String path = it.next();
                        if (!persistentPaths.contains(path))
                            nonPersistentPaths.add(path);
                    }

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

    protected void updateSubflowDependencies(Flow flow) {
        for (Flow sampleFlow : SAMPLE_FLOWS.values()) {
            Node[] subflowNodes = sampleFlow.findSubflowNodes();

            boolean subflowMatch = false;
            for (Node subflowNode : subflowNodes) {
                if (flow.getId().equals(subflowNode.getSubflowId())) {
                    subflowMatch = true;
                }
            }

            if (subflowMatch) {
                // TODO: This is more difficult...
            }

        }
    }

}
