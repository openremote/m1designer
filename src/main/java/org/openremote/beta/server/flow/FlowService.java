package org.openremote.beta.server.flow;

import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FlowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowService.class);

    final static protected Map<String, Flow> SAMPLE_FLOWS = new LinkedHashMap<>();
    static protected FlowDependencyResolver SAMPLE_DEPENDENCY_RESOLVER;

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
                    if (SampleEnvironmentWidget.FLOW.findSlot(slotId) != null)
                        return SAMPLE_FLOWS.get(SampleEnvironmentWidget.FLOW.getId());
                    if (SampleThermostatControl.FLOW.findSlot(slotId) != null)
                        return SAMPLE_FLOWS.get(SampleThermostatControl.FLOW.getId());
                    if (SampleTemperatureProcessor.FLOW.findSlot(slotId) != null)
                        return SAMPLE_FLOWS.get(SampleTemperatureProcessor.FLOW.getId());
                    return null;
                }
            };

            SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(
                SAMPLE_FLOWS.get(SampleEnvironmentWidget.FLOW.getId())
            );
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

    public Flow getFlow(@Header("id") String id) {
        LOG.debug("Getting sample flow: " + id);
        synchronized (SAMPLE_FLOWS) {
            return SAMPLE_FLOWS.get(id);
        }
    }

    public boolean putFlow(Flow flow) {
        LOG.debug("Putting sample flow: " + flow);
        synchronized (SAMPLE_FLOWS) {

            if (!SAMPLE_FLOWS.containsKey(flow.getId()))
                return false;

            if (flow.getDependencies().length > 0)
                throw new IllegalArgumentException("Don't send dependencies when updating a flow");

            SAMPLE_FLOWS.put(flow.getId(), flow);
            SAMPLE_DEPENDENCY_RESOLVER.populateDependencies(flow);

            return true;
        }
    }

}
