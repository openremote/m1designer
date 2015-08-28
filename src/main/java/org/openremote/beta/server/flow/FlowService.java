package org.openremote.beta.server.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.server.util.JsonUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependencyResolver;
import org.openremote.beta.shared.flow.Node;
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

    final protected RouteManagementService routeManagementService;

    public FlowService(RouteManagementService routeManagementService) {
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
                protected Flow findOwnerFlowOfSlot(String slotId) {
                    for (Flow sampleFlow : SAMPLE_FLOWS.values()) {
                        if (sampleFlow.findSlot(slotId) != null)
                            return sampleFlow;
                    }
                    return null;
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
        return new Flow("My Flow", new Identifier(IdentifierUtil.generateGlobalUniqueId()));
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

    public boolean putFlow(Flow flow) {
        LOG.debug("Putting flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            if (!SAMPLE_FLOWS.containsKey(flow.getId()))
                return false;

            if (flow.getDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when updating a flow");

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

}
