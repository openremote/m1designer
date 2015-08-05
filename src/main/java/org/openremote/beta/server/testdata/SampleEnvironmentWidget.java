package org.openremote.beta.server.testdata;

import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.util.Map;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.shared.util.Util.createMap;

public class SampleEnvironmentWidget {

    /* ###################################################################################### */

    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), Node.TYPE_SENSOR), LIVINGROOM_TEMPERATURE_SENSOR_SOURCE, LIVINGROOM_TEMPERATURE_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 50);
        LIVINGROOM_TEMPERATURE_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), Node.TYPE_SENSOR), LIVINGROOM_SETPOINT_SENSOR_SOURCE, LIVINGROOM_SETPOINT_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 150);
        LIVINGROOM_SETPOINT_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node LIVINGROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 400);
        editor.put("y", 100);
        LIVINGROOM_THERMOSTAT.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node LIVINGROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), Node.TYPE_ACTUATOR), LIVINGROOM_SETPOINT_ACTUATOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:livingroomSetpointActuator");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 750);
        editor.put("y", 100);
        LIVINGROOM_SETPOINT_ACTUATOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_TEMPERATURE_SENSOR = new Node("Bedroom Temperature", new Identifier(generateGlobalUniqueId(), Node.TYPE_SENSOR), BEDROOM_TEMPERATURE_SENSOR_SOURCE, BEDROOM_TEMPERATURE_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 300);
        BEDROOM_TEMPERATURE_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_SETPOINT_SENSOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), Node.TYPE_SENSOR), BEDROOM_SETPOINT_SENSOR_SOURCE, BEDROOM_SETPOINT_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 400);
        BEDROOM_SETPOINT_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node BEDROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), BEDROOM_THERMOSTAT_TEMPERATURE_SINK, BEDROOM_THERMOSTAT_SETPOINT_SINK, BEDROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 400);
        editor.put("y", 350);
        BEDROOM_THERMOSTAT.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node BEDROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), Node.TYPE_ACTUATOR), BEDROOM_SETPOINT_ACTUATOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:bedroomSetpointActuator");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 750);
        editor.put("y", 350);
        BEDROOM_SETPOINT_ACTUATOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        LIVINGROOM_TEMPERATURE_SENSOR,
        LIVINGROOM_SETPOINT_SENSOR,
        LIVINGROOM_THERMOSTAT,
        LIVINGROOM_SETPOINT_ACTUATOR,
        BEDROOM_TEMPERATURE_SENSOR,
        BEDROOM_SETPOINT_SENSOR,
        BEDROOM_THERMOSTAT,
        BEDROOM_SETPOINT_ACTUATOR
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "Environment Widget",
        new Identifier(generateGlobalUniqueId(), Flow.TYPE),
        FLOW_NODES,
        new Wire[]{
            new Wire(LIVINGROOM_TEMPERATURE_SENSOR_SOURCE, LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK),
            new Wire(LIVINGROOM_SETPOINT_SENSOR_SOURCE, LIVINGROOM_THERMOSTAT_SETPOINT_SINK),
            new Wire(LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE, LIVINGROOM_SETPOINT_ACTUATOR_SINK),
            new Wire(BEDROOM_TEMPERATURE_SENSOR_SOURCE, BEDROOM_THERMOSTAT_TEMPERATURE_SINK),
            new Wire(BEDROOM_SETPOINT_SENSOR_SOURCE, BEDROOM_THERMOSTAT_SETPOINT_SINK),
            new Wire(BEDROOM_THERMOSTAT_SETPOINT_SOURCE, BEDROOM_SETPOINT_ACTUATOR_SINK)
        }
    );

}
