package org.openremote.beta.server.testdata;

import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.util.Map;

import static org.openremote.beta.server.testdata.SampleColors.*;
import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.shared.util.Util.createMap;

public class SampleLivingroomEnvironment {

    /* ###################################################################################### */

    public static Slot TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), "ZWave Sensor"), TEMPERATURE_SENSOR_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 50);
        editor.put("color", SENSOR_COLOR);
        TEMPERATURE_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), "ZWave Sensor"), SETPOINT_SENSOR_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 150);
        editor.put("color", SENSOR_COLOR);
        SETPOINT_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot THERMOSTAT_CONTROL_FLOW_CURRENT_SINK = new Slot("Current", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot THERMOSTAT_CONTROL_FLOW_SETPOINT_SINK = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot THERMOSTAT_CONTROL_SETPOINT_SOURCE = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node THERMOSTAT_CONTROL_FLOW = new Node("Thermostat Control", new Identifier(generateGlobalUniqueId(), "Flow"), THERMOSTAT_CONTROL_FLOW_CURRENT_SINK, THERMOSTAT_CONTROL_FLOW_SETPOINT_SINK, THERMOSTAT_CONTROL_SETPOINT_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", VIRTUAL_COLOR);
        editor.put("x", 400);
        editor.put("y", 100);
        THERMOSTAT_CONTROL_FLOW.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), "ZWave Actuator"), SETPOINT_ACTUATOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", ACTUATOR_COLOR);
        editor.put("x", 800);
        editor.put("y", 150);
        SETPOINT_ACTUATOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        TEMPERATURE_SENSOR,
        SETPOINT_SENSOR,
        THERMOSTAT_CONTROL_FLOW,
        SETPOINT_ACTUATOR
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "Livingroom Environment",
        new Identifier(generateGlobalUniqueId(), Flow.TYPE),
        FLOW_NODES,
        new Wire[]{
            new Wire(TEMPERATURE_SENSOR_SOURCE, THERMOSTAT_CONTROL_FLOW_CURRENT_SINK),
            new Wire(SETPOINT_SENSOR_SOURCE, THERMOSTAT_CONTROL_FLOW_SETPOINT_SINK),
            new Wire(THERMOSTAT_CONTROL_SETPOINT_SOURCE, SETPOINT_ACTUATOR_SINK)
        }
    );

}
