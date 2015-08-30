package org.openremote.beta.test;

import org.openremote.beta.server.flow.FlowService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.*;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class FlowModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowModelTest.class);

    Flow sampleEnvironmentWidget;
    Flow sampleThermostatControl;
    Flow sampleTemperatureProcessor;
    Map<String, Flow> sampleFlows = new HashMap<>();
    FlowDependencyResolver flowDependencyResolver;

    @BeforeMethod
    public void prepareFlows() {
        sampleFlows.clear();

        sampleEnvironmentWidget = SampleEnvironmentWidget.getCopy();
        sampleFlows.put(sampleEnvironmentWidget.getId(), sampleEnvironmentWidget);
        sampleThermostatControl = SampleThermostatControl.getCopy();
        sampleFlows.put(sampleThermostatControl.getId(), sampleThermostatControl);
        sampleTemperatureProcessor = SampleTemperatureProcessor.getCopy();
        sampleFlows.put(sampleTemperatureProcessor.getId(), sampleTemperatureProcessor);

        flowDependencyResolver = new FlowService.MapFlowDependencyResolver(sampleFlows);
    }

    @Test
    public void duplicateWires() throws Exception {
        Flow flow = new Flow("Test Flow", new Identifier("123"));
        flow.addWire(new Wire("a", "b"));
        flow.addWire(new Wire("a", "b"));
        assertEquals(flow.getWires().length, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void duplicateWiresException() throws Exception {
        new Flow("foo", new Identifier("123"), new Node[0], new Wire[]{new Wire("a", "b"), new Wire("a", "b")});
    }

    @Test
    public void resolveSuperDependencies() throws Exception {

        flowDependencyResolver.populateSuperDependencies(sampleTemperatureProcessor);

        assertTrue(sampleTemperatureProcessor.hasDirectWiredSuperDependencies());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies().length, 2);

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLabel(), sampleEnvironmentWidget.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getIdentifier(), sampleEnvironmentWidget.getIdentifier());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[0].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLevel(), 1);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[0].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[0].isPeersInvalid());

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getIdentifier(), sampleThermostatControl.getIdentifier());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[1].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLevel(), 0);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[1].isPeersInvalid());

        // This will break the last (direct) super-dependency, we remove a consumer node
        sampleTemperatureProcessor.removeNode(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER);

        flowDependencyResolver.populateSuperDependencies(sampleTemperatureProcessor);

        assertTrue(sampleTemperatureProcessor.hasDirectWiredSuperDependencies());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies().length, 2);

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLabel(), sampleEnvironmentWidget.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getIdentifier(), sampleEnvironmentWidget.getIdentifier());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[0].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLevel(), 1);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[0].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[0].isPeersInvalid());

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getIdentifier(), sampleThermostatControl.getIdentifier());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[1].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLevel(), 0);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isWired());
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isPeersInvalid()); // Now broken!
    }

    @Test
    public void resolveSubDependencies() throws Exception {

        flowDependencyResolver.populateSubDependencies(sampleEnvironmentWidget, false);

        assertEquals(sampleEnvironmentWidget.getSubDependencies().length, 2);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getIdentifier(), sampleThermostatControl.getIdentifier());
        assertNull(sampleEnvironmentWidget.getSubDependencies()[0].getFlow());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getLevel(), 0);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getLabel(), sampleTemperatureProcessor.getLabel());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getIdentifier(), sampleTemperatureProcessor.getIdentifier());
        assertNull(sampleEnvironmentWidget.getSubDependencies()[1].getFlow());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getLevel(), 1);

        assertEquals(sampleEnvironmentWidget.findSubDependency(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).getIdentifier(), SampleThermostatControl.FLOW.getIdentifier());
        assertEquals(sampleEnvironmentWidget.findSubDependency(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW).getIdentifier(), SampleTemperatureProcessor.FLOW.getIdentifier());
    }

    @Test
    public void resolveSubDependenciesHydrate() throws Exception {

        flowDependencyResolver.populateSubDependencies(sampleEnvironmentWidget, true);

        assertEquals(sampleEnvironmentWidget.getSubDependencies().length, 2);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getFlow().getIdentifier(), sampleThermostatControl.getIdentifier());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getFlow().getIdentifier(), sampleTemperatureProcessor.getIdentifier());
    }

    @Test
    public void findNodesOfType() {
        assertEquals(
            sampleEnvironmentWidget.findNodes(Node.TYPE_SUBFLOW).length,
            2
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