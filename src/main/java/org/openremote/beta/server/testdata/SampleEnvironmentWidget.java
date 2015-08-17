package org.openremote.beta.server.testdata;

import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.server.route.ActuatorRoute;
import org.openremote.beta.server.route.ConsumerRoute;
import org.openremote.beta.server.route.SensorRoute;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.shared.flow.*;
import org.openremote.beta.shared.model.Identifier;
import org.openremote.beta.shared.model.Properties;
import org.openremote.beta.shared.widget.Composite;
import org.openremote.beta.shared.widget.Widget;

import java.io.IOException;
import java.util.Map;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;
import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_X;
import static org.openremote.beta.shared.flow.Node.EDITOR_PROPERTY_Y;
import static org.openremote.beta.shared.flow.Node.PROPERTY_POST_ENDPOINT;

public class SampleEnvironmentWidget {

    /* ###################################################################################### */

    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_TEMPERATURE_SENSOR_SOURCE, LIVINGROOM_TEMPERATURE_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(LIVINGROOM_TEMPERATURE_SENSOR);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_SENSOR_SOURCE, LIVINGROOM_SETPOINT_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(LIVINGROOM_SETPOINT_SENSOR);
        LIVINGROOM_SETPOINT_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        LIVINGROOM_SETPOINT_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 150);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node LIVINGROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        new SubflowRoute.Descriptor().initialize(LIVINGROOM_THERMOSTAT);
        LIVINGROOM_THERMOSTAT.getEditorProperties().put(EDITOR_PROPERTY_X, 400);
        LIVINGROOM_THERMOSTAT.getEditorProperties().put(EDITOR_PROPERTY_Y, 100);
        Widget.getWidgetDefaults(LIVINGROOM_THERMOSTAT).put(Widget.PROPERTY_POSITION_X, 10);
        Widget.getWidgetDefaults(LIVINGROOM_THERMOSTAT).put(Widget.PROPERTY_POSITION_Y, 10);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node LIVINGROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_ACTUATOR_SINK);

    static {
        new ActuatorRoute.Descriptor().initialize(LIVINGROOM_SETPOINT_ACTUATOR);
        LIVINGROOM_SETPOINT_ACTUATOR.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:livingroomSetpointActuator");
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 100);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_TEMPERATURE_SENSOR = new Node("Bedroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_TEMPERATURE_SENSOR_SOURCE, BEDROOM_TEMPERATURE_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(BEDROOM_TEMPERATURE_SENSOR);
        BEDROOM_TEMPERATURE_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        BEDROOM_TEMPERATURE_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 300);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_SETPOINT_SENSOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_SETPOINT_SENSOR_SOURCE, BEDROOM_SETPOINT_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(BEDROOM_SETPOINT_SENSOR);
        BEDROOM_SETPOINT_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        BEDROOM_SETPOINT_SENSOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 400);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node BEDROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), BEDROOM_THERMOSTAT_TEMPERATURE_SINK, BEDROOM_THERMOSTAT_SETPOINT_SINK, BEDROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        new SubflowRoute.Descriptor().initialize(BEDROOM_THERMOSTAT);
        BEDROOM_THERMOSTAT.getEditorProperties().put(EDITOR_PROPERTY_X, 400);
        BEDROOM_THERMOSTAT.getEditorProperties().put(EDITOR_PROPERTY_Y, 350);
        Widget.getWidgetDefaults(BEDROOM_THERMOSTAT).put(Widget.PROPERTY_POSITION_X, 10);
        Widget.getWidgetDefaults(BEDROOM_THERMOSTAT).put(Widget.PROPERTY_POSITION_Y, 100);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node BEDROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), BEDROOM_SETPOINT_ACTUATOR_SINK);

    static {
        new ActuatorRoute.Descriptor().initialize(BEDROOM_SETPOINT_ACTUATOR);
        BEDROOM_SETPOINT_ACTUATOR.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:bedroomSetpointActuator");
        BEDROOM_SETPOINT_ACTUATOR.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        BEDROOM_SETPOINT_ACTUATOR.getEditorProperties().put(EDITOR_PROPERTY_Y, 350);
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
