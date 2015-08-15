package org.openremote.beta.server.testdata;

import org.openremote.beta.server.route.ActuatorRoute;
import org.openremote.beta.server.route.SensorRoute;
import org.openremote.beta.shared.flow.*;
import org.openremote.beta.shared.inventory.Actuator;
import org.openremote.beta.shared.model.Identifier;
import org.openremote.beta.shared.widget.Composite;
import org.openremote.beta.shared.widget.Widget;

import java.io.IOException;
import java.util.Map;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;
import static org.openremote.beta.shared.util.Util.createMap;

public class SampleEnvironmentWidget {

    /* ###################################################################################### */

    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_TEMPERATURE_SENSOR_SOURCE, LIVINGROOM_TEMPERATURE_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 50);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", SensorRoute.NODE_TYPE_LABEL);
        LIVINGROOM_TEMPERATURE_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_SENSOR_SOURCE, LIVINGROOM_SETPOINT_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 150);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", SensorRoute.NODE_TYPE_LABEL);
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
        editor.put("color", NodeColor.VIRTUAL);
        editor.put("typeLabel", Node.TYPE_SUBFLOW_LABEL);
        LIVINGROOM_THERMOSTAT.setProperties(properties);

        Map<String, Object> widgetProperties =
            Widget.configureProperties(LIVINGROOM_THERMOSTAT, Composite.TYPE, Composite.COMPONENT);
        widgetProperties.put("positionX", 10);
        widgetProperties.put("positionY", 10);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node LIVINGROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_ACTUATOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:livingroomSetpointActuator");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 750);
        editor.put("y", 100);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", ActuatorRoute.NODE_TYPE_LABEL);
        LIVINGROOM_SETPOINT_ACTUATOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_TEMPERATURE_SENSOR = new Node("Bedroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_TEMPERATURE_SENSOR_SOURCE, BEDROOM_TEMPERATURE_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 300);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", SensorRoute.NODE_TYPE_LABEL);
        BEDROOM_TEMPERATURE_SENSOR.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_SETPOINT_SENSOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_SETPOINT_SENSOR_SOURCE, BEDROOM_SETPOINT_SENSOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 400);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", SensorRoute.NODE_TYPE_LABEL);
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
        editor.put("color", NodeColor.VIRTUAL);
        editor.put("typeLabel", Node.TYPE_SUBFLOW_LABEL);
        BEDROOM_THERMOSTAT.setProperties(properties);

        Map<String, Object> widgetProperties =
            Widget.configureProperties(BEDROOM_THERMOSTAT, Composite.TYPE, Composite.COMPONENT);
        widgetProperties.put("positionX", 10);
        widgetProperties.put("positionY", 100);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node BEDROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), BEDROOM_SETPOINT_ACTUATOR_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:bedroomSetpointActuator");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 750);
        editor.put("y", 350);
        editor.put("color", NodeColor.SENSOR_ACTUATOR);
        editor.put("typeLabel", ActuatorRoute.NODE_TYPE_LABEL);
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

    public static Flow getCopy() {
        try {
            return JSON.readValue(JSON.writeValueAsString(FLOW), Flow.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
