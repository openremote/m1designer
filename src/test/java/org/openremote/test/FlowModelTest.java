package org.openremote.test;

import org.openremote.server.testdata.SampleEnvironmentWidget;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.FlowDependencyResolver;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Wire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class FlowModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowModelTest.class);
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

        flowDependencyResolver = new MapFlowDependencyResolver(sampleFlows) {
            @Override
            protected void stopFlowIfRunning(Flow flow) {
                // NOOP, no server running
            }

            @Override
            protected String generateGlobalUniqueId() {
                return IdentifierUtil.generateGlobalUniqueId();
            }
        };
    }

    @Test
    public void duplicateWires() throws Exception {
        Flow flow = new Flow("Test Flow", "123");
        flow.addWire(new Wire("a", "b"));
        flow.addWire(new Wire("a", "b"));
        assertEquals(flow.getWires().length, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void duplicateWiresException() throws Exception {
        new Flow("foo", "123", new Node[0], new Wire[]{new Wire("a", "b"), new Wire("a", "b")});
    }

    @Test
    public void resolveSuperDependencies() throws Exception {

        flowDependencyResolver.populateDependencies(sampleTemperatureProcessor, false);

        assertTrue(sampleTemperatureProcessor.hasDirectWiredSuperDependencies());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies().length, 2);

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLabel(), sampleEnvironmentWidget.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getId(), sampleEnvironmentWidget.getId());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[0].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLevel(), 1);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[0].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[0].isPeersInvalid());

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getId(), sampleThermostatControl.getId());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[1].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLevel(), 0);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[1].isPeersInvalid());

        // This will break the last (direct) super-dependency, we remove a consumer node
        sampleTemperatureProcessor.removeNode(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER);

        flowDependencyResolver.populateDependencies(sampleTemperatureProcessor, false);

        assertTrue(sampleTemperatureProcessor.hasDirectWiredSuperDependencies());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies().length, 2);

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLabel(), sampleEnvironmentWidget.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getId(), sampleEnvironmentWidget.getId());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[0].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[0].getLevel(), 1);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[0].isWired());
        assertFalse(sampleTemperatureProcessor.getSuperDependencies()[0].isPeersInvalid());

        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getId(), sampleThermostatControl.getId());
        assertNull(sampleTemperatureProcessor.getSuperDependencies()[1].getFlow());
        assertEquals(sampleTemperatureProcessor.getSuperDependencies()[1].getLevel(), 0);
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isWired());
        assertTrue(sampleTemperatureProcessor.getSuperDependencies()[1].isPeersInvalid()); // Now broken!
    }

    @Test
    public void resolveSubDependencies() throws Exception {

        flowDependencyResolver.populateDependencies(sampleEnvironmentWidget, false);

        assertEquals(sampleEnvironmentWidget.getSubDependencies().length, 2);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getLabel(), sampleThermostatControl.getLabel());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getId(), sampleThermostatControl.getId());
        assertNull(sampleEnvironmentWidget.getSubDependencies()[0].getFlow());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getLevel(), 0);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getLabel(), sampleTemperatureProcessor.getLabel());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getId(), sampleTemperatureProcessor.getId());
        assertNull(sampleEnvironmentWidget.getSubDependencies()[1].getFlow());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getLevel(), 1);

        assertEquals(sampleEnvironmentWidget.findSubDependency(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT).getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(sampleEnvironmentWidget.findSubDependency(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW).getId(), SampleTemperatureProcessor.FLOW.getId());
    }

    @Test
    public void resolveSubDependenciesHydrate() throws Exception {

        flowDependencyResolver.populateDependencies(sampleEnvironmentWidget, true);

        assertEquals(sampleEnvironmentWidget.getSubDependencies().length, 2);

        assertEquals(sampleEnvironmentWidget.getSubDependencies()[0].getFlow().getId(), sampleThermostatControl.getId());
        assertEquals(sampleEnvironmentWidget.getSubDependencies()[1].getFlow().getId(), sampleTemperatureProcessor.getId());
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
        assertTrue(sampleEnvironmentWidget.hasWires(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SOURCE.getId()));

        assertEquals(sampleEnvironmentWidget.findWiresForSink(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK.getId()).length, 1);

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