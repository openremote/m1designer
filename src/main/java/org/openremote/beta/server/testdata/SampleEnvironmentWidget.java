package org.openremote.beta.server.testdata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.server.route.ActuatorRoute;
import org.openremote.beta.server.route.SensorRoute;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.io.IOException;
import java.util.List;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;

public class SampleEnvironmentWidget {

    /* ###################################################################################### */

    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_TEMPERATURE_SENSOR = new Node("Livingroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE));

    static {
        new SensorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LIVINGROOM_TEMPERATURE_SENSOR_SOURCE);
                slots.add(LIVINGROOM_TEMPERATURE_SENSOR_SINK);
            }
        }.initialize(LIVINGROOM_TEMPERATURE_SENSOR);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionX((double) 50);
        LIVINGROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionY((double) 130);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot LIVINGROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node LIVINGROOM_SETPOINT_SENSOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE));

    static {
        new SensorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LIVINGROOM_SETPOINT_SENSOR_SOURCE);
                slots.add(LIVINGROOM_SETPOINT_SENSOR_SINK);
            }
        }.initialize(LIVINGROOM_SETPOINT_SENSOR);
        LIVINGROOM_SETPOINT_SENSOR.getEditorSettings().setPositionX((double) 50);
        LIVINGROOM_SETPOINT_SENSOR.getEditorSettings().setPositionY((double) 250);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK, SampleThermostatControl.TEMPERATURE_CONSUMER.getLabel());
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK, SampleThermostatControl.SETPOINT_CONSUMER.getLabel());
    public static Slot LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE, SampleThermostatControl.SETPOINT_PRODUCER.getLabel());
    public static Node LIVINGROOM_THERMOSTAT = new Node("Livingroom Thermostat", new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), SampleThermostatControl.FLOW.getId());

    static {
        new SubflowRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK);
                slots.add(LIVINGROOM_THERMOSTAT_SETPOINT_SINK);
                slots.add(LIVINGROOM_THERMOSTAT_SETPOINT_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return super.getInitialProperties()
                    .put("positionX", 30)
                    .put("positionY", 50);
            }
        }.initialize(LIVINGROOM_THERMOSTAT);
        LIVINGROOM_THERMOSTAT.getEditorSettings().setPositionX((double) 400);
        LIVINGROOM_THERMOSTAT.getEditorSettings().setPositionY((double) 200);
    }

    /* ###################################################################################### */

    public static Slot LIVINGROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node LIVINGROOM_SETPOINT_ACTUATOR = new Node("Livingroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE));

    static {
        new ActuatorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LIVINGROOM_SETPOINT_ACTUATOR_SINK);
            }
        }.initialize(LIVINGROOM_SETPOINT_ACTUATOR);
        LIVINGROOM_SETPOINT_ACTUATOR.setPostEndpoint("mock:livingroomSetpointActuator");
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionX((double) 700);
        LIVINGROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionY((double) 200);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_TEMPERATURE_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_TEMPERATURE_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_TEMPERATURE_SENSOR = new Node("Bedroom Temperature", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE));

    static {
        new SensorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(BEDROOM_TEMPERATURE_SENSOR_SOURCE);
                slots.add(BEDROOM_TEMPERATURE_SENSOR_SINK);
            }
        }.initialize(BEDROOM_TEMPERATURE_SENSOR);
        BEDROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionX((double) 50);
        BEDROOM_TEMPERATURE_SENSOR.getEditorSettings().setPositionY((double) 400);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_SENSOR_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot BEDROOM_SETPOINT_SENSOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node BEDROOM_SETPOINT_SENSOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), SensorRoute.NODE_TYPE));

    static {
        new SensorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(BEDROOM_SETPOINT_SENSOR_SOURCE);
                slots.add(BEDROOM_SETPOINT_SENSOR_SINK);
            }
        }.initialize(BEDROOM_SETPOINT_SENSOR);
        BEDROOM_SETPOINT_SENSOR.getEditorSettings().setPositionX((double) 50);
        BEDROOM_SETPOINT_SENSOR.getEditorSettings().setPositionY((double) 520);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_THERMOSTAT_TEMPERATURE_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.TEMPERATURE_CONSUMER_SINK, SampleThermostatControl.TEMPERATURE_CONSUMER.getLabel());
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SINK = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_CONSUMER_SINK, SampleThermostatControl.SETPOINT_CONSUMER.getLabel());
    public static Slot BEDROOM_THERMOSTAT_SETPOINT_SOURCE = new Slot(generateGlobalUniqueId(), SampleThermostatControl.SETPOINT_PRODUCER_SOURCE, SampleThermostatControl.SETPOINT_PRODUCER.getLabel());
    public static Node BEDROOM_THERMOSTAT = new Node("Bedroom Thermostat", new Identifier(generateGlobalUniqueId(), Node.TYPE_SUBFLOW), SampleThermostatControl.FLOW.getId());

    static {
        new SubflowRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(BEDROOM_THERMOSTAT_TEMPERATURE_SINK);
                slots.add(BEDROOM_THERMOSTAT_SETPOINT_SINK);
                slots.add(BEDROOM_THERMOSTAT_SETPOINT_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return super.getInitialProperties()
                    .put("positionX", 30)
                    .put("positionY", 200);
            }
        }.initialize(BEDROOM_THERMOSTAT);
        BEDROOM_THERMOSTAT.getEditorSettings().setPositionX((double) 400);
        BEDROOM_THERMOSTAT.getEditorSettings().setPositionY((double) 450);
    }

    /* ###################################################################################### */

    public static Slot BEDROOM_SETPOINT_ACTUATOR_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node BEDROOM_SETPOINT_ACTUATOR = new Node("Bedroom Setpoint", new Identifier(generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE));

    static {
        new ActuatorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(BEDROOM_SETPOINT_ACTUATOR_SINK);
            }
        }.initialize(BEDROOM_SETPOINT_ACTUATOR);
        BEDROOM_SETPOINT_ACTUATOR.setPostEndpoint("mock:bedroomSetpointActuator");
        BEDROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionX((double) 700);
        BEDROOM_SETPOINT_ACTUATOR.getEditorSettings().setPositionY((double) 450);
    }


    /* ###################################################################################### */

    public static Node LIVINGROOM_LABEL = new Node("Livingroom", new Identifier(generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE));

    static {
        new TextLabelNodeDescriptor() {
            @Override
            protected ObjectNode getInitialProperties() {
                return TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES.deepCopy()
                    .put("textColor", "#ddd")
                    .put("emptyValue", "Livingroom")
                    .put("fontSizePixels", 20)
                    .put("positionX", 30)
                    .put("positionY", 20);
            }
        }.initialize(LIVINGROOM_LABEL);
        LIVINGROOM_LABEL.getEditorSettings().setPositionX((double) 950);
        LIVINGROOM_LABEL.getEditorSettings().setPositionY((double) 80);
    }

    /* ###################################################################################### */

    public static Node BEDROOM_LABEL = new Node("Bedroom", new Identifier(generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE));

    static {
        new TextLabelNodeDescriptor() {
            @Override
            protected ObjectNode getInitialProperties() {
                return TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES.deepCopy()
                    .put("textColor", "#ddd")
                    .put("emptyValue", "Bedroom")
                    .put("fontSizePixels", 20)
                    .put("positionX", 30)
                    .put("positionY", 170);
            }
        }.initialize(BEDROOM_LABEL);
        BEDROOM_LABEL.getEditorSettings().setPositionX((double) 950);
        BEDROOM_LABEL.getEditorSettings().setPositionY((double) 380);
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
        BEDROOM_SETPOINT_ACTUATOR,
        LIVINGROOM_LABEL,
        BEDROOM_LABEL

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
