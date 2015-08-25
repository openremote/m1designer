package org.openremote.beta.server.testdata;

import org.openremote.beta.server.catalog.WidgetProperties;
import org.openremote.beta.server.route.ActuatorRoute;
import org.openremote.beta.server.route.SensorRoute;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.io.IOException;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;

public class SampleEnvironmentWidget {

    /* ###################################################################################### */

    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_TEMPERATURE_SENSOR_SOURCE, LIVINGROOM_TEMPERATURE_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(LIVINGROOM_TEMPERATURE_SENSOR);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionX((double)50);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionY((double)130);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_SENSOR_SOURCE, LIVINGROOM_SETPOINT_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(LIVINGROOM_SETPOINT_SENSOR);
        LIVINGROOM_SETPOINT_SENSOR.getEditorSettings().setPositionX((double)50);
        LIVINGROOM_SETPOINT_SENSOR.getEditorSettings().setPositionY((double)250);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node LIVINGROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SINK, LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        new SubflowRoute.Descriptor() {
            @Override
            protected Object getInitialProperties() {
                WidgetProperties widgetProperties = (WidgetProperties) super.getInitialProperties();
                widgetProperties.setPositionX(10);
                widgetProperties.setPositionY(10);
                return widgetProperties;
            }
        }.initialize(LIVINGROOM_THERMOSTAT);
        LIVINGROOM_THERMOSTAT.getEditorSettings().setPositionX((double)400);
        LIVINGROOM_THERMOSTAT.getEditorSettings().setPositionY((double)200);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node LIVINGROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), LIVINGROOM_SETPOINT_ACTUATOR_SINK);

    static {
        new ActuatorRoute.Descriptor().initialize(LIVINGROOM_SETPOINT_ACTUATOR);
        LIVINGROOM_SETPOINT_ACTUATOR.setPostEndpoint("mock:livingroomSetpointActuator");
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionX((double)750);
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionY((double)200);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_TEMPERATURE_SENSOR = new Node("Bedroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_TEMPERATURE_SENSOR_SOURCE, BEDROOM_TEMPERATURE_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(BEDROOM_TEMPERATURE_SENSOR);
        BEDROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionX((double)50);
        BEDROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionY((double)400);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_SETPOINT_SENSOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE), BEDROOM_SETPOINT_SENSOR_SOURCE, BEDROOM_SETPOINT_SENSOR_SINK);

    static {
        new SensorRoute.Descriptor().initialize(BEDROOM_SETPOINT_SENSOR);
        BEDROOM_SETPOINT_SENSOR.getEditorSettings().setPositionX((double)50);
        BEDROOM_SETPOINT_SENSOR.getEditorSettings().setPositionY((double)520);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK);
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE);
    public static Node BEDROOM_THERMOSTAT = new Node(SampleThermostatControl.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), BEDROOM_THERMOSTAT_TEMPERATURE_SINK, BEDROOM_THERMOSTAT_SETPOINT_SINK, BEDROOM_THERMOSTAT_SETPOINT_SOURCE);

    static {
        new SubflowRoute.Descriptor() {
            @Override
            protected Object getInitialProperties() {
                WidgetProperties widgetProperties = (WidgetProperties) super.getInitialProperties();
                widgetProperties.setPositionX(10);
                widgetProperties.setPositionY(100);
                return widgetProperties;
            }
        }.initialize(BEDROOM_THERMOSTAT);
        BEDROOM_THERMOSTAT.getEditorSettings().setPositionX((double)400);
        BEDROOM_THERMOSTAT.getEditorSettings().setPositionY((double)450);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node BEDROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE), BEDROOM_SETPOINT_ACTUATOR_SINK);

    static {
        new ActuatorRoute.Descriptor().initialize(BEDROOM_SETPOINT_ACTUATOR);
        BEDROOM_SETPOINT_ACTUATOR.setPostEndpoint("mock:bedroomSetpointActuator");
        BEDROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionX((double)750);
        BEDROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionY((double)450);
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
