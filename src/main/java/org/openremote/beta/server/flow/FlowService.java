package org.openremote.beta.server.flow;

import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;

import java.util.*;

public class FlowService implements StaticService {

    final static protected Map<String, Flow> flows = new LinkedHashMap<>();

    @Override
    public void start() throws Exception {
        synchronized (flows) {
            // TODO sample data
            flows.put(SampleEnvironmentWidget.FLOW.getId(), SampleEnvironmentWidget.FLOW);
            flows.put(SampleTemperatureProcessor.FLOW.getId(), SampleTemperatureProcessor.FLOW);
            flows.put(SampleThermostatControl.FLOW.getId(), SampleThermostatControl.FLOW);
        }
    }

    @Override
    public void stop() throws Exception {
        synchronized (flows) {
            flows.clear();
        }
    }

    public Flow[] getFlows() {
        synchronized (flows) {
            // Shallow copy, we only want the label and id of the flow
            Flow[] flowsInfo = new Flow[flows.size()];
            int i = 0;
            for (Flow flow : flows.values()) {
                flowsInfo[i++] = new Flow(flow.getLabel(), flow.getIdentifier());
            }
            return flowsInfo;
        }
    }

    public Flow getFlow(@Header("id") String id) {
        synchronized (flows) {
            return flows.get(id);
        }
    }

    public boolean putFlow(Flow flow) {
        synchronized (flows) {
            if (!flows.containsKey(flow.getId()))
                return false;
            flows.put(flow.getId(), flow);
            return true;
        }
    }

}
