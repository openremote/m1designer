package org.openremote.server.testdata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.route.ActuatorRoute;
import org.openremote.server.route.SensorRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.flow.Wire;

import java.io.IOException;
import java.util.List;

import static org.openremote.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.server.util.JsonUtil.JSON;

public class ExampleLight {

    /* ###################################################################################### */

    public static Slot LIGHT_SENSOR_SOURCE = new Slot(generateGlobalUniqueId(), Slot.TYPE_SOURCE);
    public static Slot LIGHT_SENSOR_SINK = new Slot(generateGlobalUniqueId(), Slot.TYPE_SINK, false);
    public static Node LIGHT_SENSOR = new Node("Light Switch Status", generateGlobalUniqueId(), SensorRoute.NODE_TYPE);

    static {
        new SensorRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LIGHT_SENSOR_SOURCE);
                slots.add(LIGHT_SENSOR_SINK);
            }

            @Override
            protected void addPersistentPropertyPaths(List<String> propertyPaths) {
                super.addPersistentPropertyPaths(propertyPaths);
                propertyPaths.add("discoveryEndpoint");
                propertyPaths.add("consumerEndpoint");
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return JSON.createObjectNode()
                    .put("discoveryEndpoint", "zwave://discovery?serialPort={{env:ZWAVE_SERIAL_PORT}}")
                    .put("consumerEndpoint", "zwave://3?serialPort={{env:ZWAVE_SERIAL_PORT}}&command=STATUS");
            }
        }.initialize(LIGHT_SENSOR);

        LIGHT_SENSOR.getEditorSettings().setPositionX((double) 50);
        LIGHT_SENSOR.getEditorSettings().setPositionY((double) 130);
    }

    /* ###################################################################################### */

    public static Node LIGHT_ACTUATOR = new Node("Light Switch Actuator", generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE);

    static {
        new ActuatorRoute.Descriptor() {
            @Override
            protected ObjectNode getInitialProperties() {
                return JSON.createObjectNode()
                    .put("producerEndpoint", "zwave://3?serialPort={{env:ZWAVE_SERIAL_PORT}}")
                    .put("zwaveCommand", "DIM");
            }

        }.initialize(LIGHT_ACTUATOR);

        LIGHT_ACTUATOR.getEditorSettings().setPositionX((double) 300);
        LIGHT_ACTUATOR.getEditorSettings().setPositionY((double) 130);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        //LIGHT_SENSOR,
        LIGHT_ACTUATOR
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "Example Light",
        generateGlobalUniqueId(),
        FLOW_NODES,
        new Wire[0]
    );

    public static Flow getCopy() {
        try {
            return JSON.readValue(JSON.writeValueAsString(FLOW), Flow.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
