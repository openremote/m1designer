package org.openremote.beta.server.flow;

import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;

import java.util.ArrayList;
import java.util.List;

public class FlowService implements StaticService {

    final static protected List<Flow> flows = new ArrayList<>();

    @Override
    public void start() throws Exception {
        synchronized (flows) {
            // TODO sample data
            flows.add(SampleEnvironmentWidget.FLOW);
            flows.add(SampleTemperatureProcessor.FLOW);
            flows.add(SampleThermostatControl.FLOW);
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
            for (int i = 0; i < flowsInfo.length; i++) {
                Flow flow = flows.get(i);
                flowsInfo[i] = new Flow(flow.getLabel(), flow.getIdentifier());
            }
            return flowsInfo;
        }
    }

    public Flow getFlow(@Header("id") String id) {
        synchronized (flows) {
            for (Flow flow : flows) {
                if (flow.getIdentifier().getId().equals(id)) {
                    return flow;
                }
            }
        }
        return null;
    }

}
