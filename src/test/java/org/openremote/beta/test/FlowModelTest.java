package org.openremote.beta.test;

import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.*;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FlowModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowModelTest.class);

    Flow sampleEnvironmentWidget;
    Flow sampleThermostatControl;
    Flow sampleTemperatureProcessor;

    @BeforeMethod
    public void prepareFlows() {
        sampleEnvironmentWidget = SampleEnvironmentWidget.getCopy();
        sampleThermostatControl = SampleThermostatControl.getCopy();
        sampleTemperatureProcessor = SampleTemperatureProcessor.getCopy();

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
    }

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

        // Peer label resolution (a subflow's slots must have the same labels as the target flows' consumer/producer nodes)
        assertEquals(
            sampleEnvironmentWidget.findSlot(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK.getId()).getLabel(),
            sampleThermostatControl.findNode(SampleThermostatControl.TEMPERATURE_CONSUMER.getId()).getLabel()
        );

        assertTrue(sampleEnvironmentWidget.hasDependency(sampleThermostatControl.getId()));
        assertTrue(sampleEnvironmentWidget.hasDependency(sampleTemperatureProcessor.getId()));
        assertTrue(sampleThermostatControl.hasDependency(sampleTemperatureProcessor.getId()));
        assertFalse(sampleTemperatureProcessor.hasDependency(sampleThermostatControl.getId()));
        assertFalse(sampleTemperatureProcessor.hasDependency(sampleEnvironmentWidget.getId()));
    }

    @Test
    public void findNodesOfType() {
        assertEquals(
            sampleEnvironmentWidget.findNodes(Node.TYPE_SUBFLOW).length,
            2
        );
    }

    @Test
    public void findNodeInAllFlows() {
        assertEquals(
            sampleEnvironmentWidget.findNodeInAllFlows(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId()).getId(),
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId()
        );
        assertEquals(
            sampleEnvironmentWidget.findNodeInAllFlows(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getId()).getId(),
            SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getId()
        );
    }

    @Test
    public void findWidgetNodes() {
        assertEquals(
            sampleThermostatControl.findClientWidgetNodes().length,
            6
        );
    }

    @Test
    public void findSubflow() {
        assertEquals(
            sampleEnvironmentWidget.findSubflow(sampleEnvironmentWidget.findNode(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId())).getId(),
            sampleThermostatControl.getId()
        );
    }

    @Test
    public void findSlot() {
        assertEquals(
            sampleEnvironmentWidget.findSlotInAllFlows(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK.getId()).getId(),
            SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK.getId()
        );

        assertEquals(
            sampleEnvironmentWidget.findNode(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR.getId()).findConnectableSlots(Slot.TYPE_SOURCE).length,
            1
        );
        assertEquals(
            sampleEnvironmentWidget.findNode(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR.getId()).findConnectableSlots(Slot.TYPE_SINK).length,
            0
        );
    }

    @Test
    public void removeWire() {
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 1);
        sampleEnvironmentWidget.removeWireBetweenSlots(
            SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SOURCE,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK
        );
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 0);
    }

    @Test
    public void removeNode() {
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 1);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 1);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 1);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.BEDROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.BEDROOM_THERMOSTAT).length, 1);

        Node livingroomThermostat = sampleEnvironmentWidget.findNode(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId());
        assertEquals(sampleEnvironmentWidget.removeNode(livingroomThermostat).length, 3);

        assertNull(sampleEnvironmentWidget.findNode(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId()));
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 0);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 0);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR, SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).length, 0);
        assertEquals(sampleEnvironmentWidget.findWiresBetween(SampleEnvironmentWidget.BEDROOM_TEMPERATURE_SENSOR, SampleEnvironmentWidget.BEDROOM_THERMOSTAT).length, 1);
    }

}