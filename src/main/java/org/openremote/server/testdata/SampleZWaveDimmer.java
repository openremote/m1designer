package org.openremote.server.testdata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.catalog.widget.SliderNodeDescriptor;
import org.openremote.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.server.route.ActuatorRoute;
import org.openremote.server.route.SensorRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Wire;

import java.util.List;

import static org.openremote.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.server.util.JsonUtil.JSON;

public class SampleZWaveDimmer {

    /* ###################################################################################### */

    public static Node DIMMER_SENSOR = new Node("Dimmer", generateGlobalUniqueId(), SensorRoute.NODE_TYPE);

    static {
        new SensorRoute.Descriptor() {
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
                    .put("consumerEndpoint", "zwave://{{env:SAMPLE_ZWAVE_DIMMER_NODE_ID}}?serialPort={{env:ZWAVE_SERIAL_PORT}}&command=STATUS");
            }
        }.initialize(DIMMER_SENSOR);

        DIMMER_SENSOR.getEditorSettings().setPositionX((double) 50);
        DIMMER_SENSOR.getEditorSettings().setPositionY((double) 130);
    }

    /* ###################################################################################### */

    public static Node DIMMER_ACTUATOR = new Node("Dimmer", generateGlobalUniqueId(), ActuatorRoute.NODE_TYPE);

    static {
        new ActuatorRoute.Descriptor() {
            @Override
            protected ObjectNode getInitialProperties() {
                return JSON.createObjectNode()
                    .put("producerEndpoint", "zwave://{{env:SAMPLE_ZWAVE_DIMMER_NODE_ID}}?serialPort={{env:ZWAVE_SERIAL_PORT}}&command=DIM");
            }

        }.initialize(DIMMER_ACTUATOR);

        DIMMER_ACTUATOR.getEditorSettings().setPositionX((double) 400);
        DIMMER_ACTUATOR.getEditorSettings().setPositionY((double) 430);
    }

    /* ###################################################################################### */

    public static Node DIMMER_STATUS = new Node("Dim Status", generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE);

    static {
        new TextLabelNodeDescriptor() {
            @Override
            protected ObjectNode getInitialProperties() {
                return TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES.deepCopy()
                    .put("textColor", "#ddd")
                    .put("emptyValue", "Waiting for dim status...")
                    .put("fontSizePixels", 20)
                    .put("positionX", 30)
                    .put("positionY", 80);
            }
        }.initialize(DIMMER_STATUS);
        DIMMER_STATUS.getEditorSettings().setPositionX((double) 300);
        DIMMER_STATUS.getEditorSettings().setPositionY((double) 30);
    }

    /* ###################################################################################### */

    public static Node DIMMER_LEVEL = new Node("Dim Level", generateGlobalUniqueId(), SliderNodeDescriptor.TYPE);

    static {
        new SliderNodeDescriptor().initialize(DIMMER_LEVEL);
        DIMMER_LEVEL.getEditorSettings().setPositionX((double) 100);
        DIMMER_LEVEL.getEditorSettings().setPositionY((double) 350);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        DIMMER_SENSOR,
        DIMMER_ACTUATOR,
        DIMMER_STATUS,
        DIMMER_LEVEL
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "ZWave Dimmer",
        generateGlobalUniqueId(),
        FLOW_NODES,
        new Wire[] {
            new Wire(DIMMER_SENSOR.getSlots()[0], DIMMER_STATUS.getSlots()[0]),
            new Wire(DIMMER_LEVEL.getSlots()[1], DIMMER_ACTUATOR.getSlots()[0])
        }
    );
}
