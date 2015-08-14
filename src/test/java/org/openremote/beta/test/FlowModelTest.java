package org.openremote.beta.test;

import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.FlowDependencyResolver;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FlowModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowModelTest.class);

    @Test
    public void duplicateWires() throws Exception {
        Flow flow = new Flow();
        flow.addWire(new Wire("a", "b"));
        flow.addWire(new Wire("a", "b"));
        assertEquals(flow.getWires().length, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void duplicateWiresException() throws Exception {
        new Flow("foo", new Identifier("123"), new Node[0], new Wire[]{new Wire("a", "b"), new Wire("a", "b")});
    }

    @Test
    public void resolveDependencies() throws Exception {
        Flow sampleEnvironmentWidget = SampleEnvironmentWidget.getCopy();
        Flow sampleThermostatControl = SampleThermostatControl.getCopy();
        Flow sampleTemperatureProcessor = SampleTemperatureProcessor.getCopy();

        FlowDependencyResolver dependencyResolver = new FlowDependencyResolver() {
            @Override
            protected Flow findOwnerFlowOfSlot(String slotId) {
                if (sampleTemperatureProcessor.findSlot(slotId) != null)
                    return sampleTemperatureProcessor;
                if (sampleThermostatControl.findSlot(slotId) != null)
                    return sampleThermostatControl;
                if (sampleEnvironmentWidget.findSlot(slotId) != null)
                    return sampleEnvironmentWidget;
                return null;
            }
        };

        dependencyResolver.populateDependencies(sampleEnvironmentWidget);

        assertNull(sampleEnvironmentWidget.findOwnerFlowOfSlot("123"));

        assertEquals(
            sampleEnvironmentWidget.findOwnerFlowOfSlot(SampleThermostatControl.TEMPERATURE_CONSUMER_SINK.getId()),
            sampleThermostatControl
        );
        assertEquals(
            sampleEnvironmentWidget.findOwnerFlowOfSlot(SampleThermostatControl.SETPOINT_CONSUMER_SINK.getId()),
            sampleThermostatControl
        );
        assertEquals(
            sampleEnvironmentWidget.findOwnerFlowOfSlot(SampleThermostatControl.SETPOINT_PRODUCER_SOURCE.getId()),
            sampleThermostatControl
        );

        assertEquals(
            sampleEnvironmentWidget.findOwnerFlowOfSlot(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK.getId()),
            sampleTemperatureProcessor
        );
        assertEquals(
            sampleEnvironmentWidget.findOwnerFlowOfSlot(SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE.getId()),
            sampleTemperatureProcessor
        );
    }

}
