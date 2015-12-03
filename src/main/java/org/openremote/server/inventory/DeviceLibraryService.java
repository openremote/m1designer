/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.inventory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.StaticService;
import org.openremote.server.util.UrlUtil;
import org.openremote.shared.inventory.Adapter;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.openremote.protocol.zwave.model.commandclasses.DeviceDiscoveryCommandClassVisitor.*;
import static org.openremote.server.util.JsonUtil.JSON;

// TODO Part of this class should be moved to zwave project

public class DeviceLibraryService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceLibraryService.class);

    protected final CamelContext context;

    public DeviceLibraryService(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    public Device[] initializeDevices(Adapter adapter, List<Device> devices) throws Exception {

        // Parse the properties once
        ObjectNode adapterProperties = JSON.readValue(adapter.getProperties(), ObjectNode.class);
        Map<Device, ObjectNode> deviceProperties = new HashMap<>();
        for (Device device : devices) {
            deviceProperties.put(device, getProperties(device));
        }

        if (adapter.getId().equals("zwave")) {
            return initializeZWaveDevices(adapterProperties, deviceProperties, devices);
        }
        return devices.toArray(new Device[devices.size()]);
    }

    protected ObjectNode getProperties(Device device) throws Exception {
        return device.getProperties() != null
            ? JSON.readValue(device.getProperties(), ObjectNode.class)
            : JSON.createObjectNode();

    }

    protected boolean isTrue(ObjectNode properties, String propertyName) {
        return properties.has(propertyName) && properties.get(propertyName).asBoolean();
    }

    protected Device[] initializeZWaveDevices(ObjectNode adapterProperties, Map<Device, ObjectNode> deviceProperties, List<Device> devices) {

        List<Device> result = new ArrayList<>();

        // Find all root devices
        for (Device device : devices) {
            if (isTrue(deviceProperties.get(device), ATTR_NAME_IS_ROOT)) {

                LOG.debug("Found root device: " + device);

                String nodeId = deviceProperties.get(device).get(ATTR_NAME_NODE_ID).asText();
                String productTypeId = deviceProperties.get(device).get(ATTR_NAME_PRODUCT_TYPE_ID).asText();
                String productId = deviceProperties.get(device).get(ATTR_NAME_PRODUCT_ID).asText();
                String serialPort = adapterProperties.get("serialPort").get("value").asText();

                // TODO: Yeah, this is not great
                if (nodeId.length() > 0 && productTypeId.equals("0x0018") && productId.equals("0x0100")) {
                    // Benext Dimmer
                    device.setStatus(Device.Status.READY);
                    device.setLabel("Benext Dimmer#" + nodeId);
                    device.setSensorEndpoints(new String[]{
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "FREQUENCY_SCALE_HZ").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "ELECTRIC_METER_SCALE_W").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "ELECTRIC_METER_SCALE_KWH").toString()
                    });
                    device.setActuatorEndpoints(new String[]{
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "DIM").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "ON").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "OFF").toString()
                    });
                } else if (nodeId.length() > 0 && productTypeId.equals("0x0002") && productId.equals("0x0064")) {
                    // Aeotec Multisensor 6
                    device.setStatus(Device.Status.READY);
                    device.setLabel("Aeotec MultiSensor 6#" + nodeId);
                    device.setSensorEndpoints(new String[]{
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "AIR_TEMPERATURE_SCALE_CELSIUS").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "AIR_TEMPERATURE_SCALE_FAHRENHEIT").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "HUMIDITY_SCALE_PERCENTAGE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "LUMINANCE_SCALE_LUX").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "ULTRAVIOLET_SCALE_UV_INDEX").toString(),
                    });
                } else if (nodeId.length() > 0 && productTypeId.equals("0x0002") && productId.equals("0x0002")) {
                    // Zipato RGBW Bulb
                    device.setStatus(Device.Status.READY);
                    device.setLabel("Zipato RGBW Bulb#" + nodeId);
                    device.setSensorEndpoints(new String[]{
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS_COLOR_CHANNEL_WARM_WHITE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS_COLOR_CHANNEL_COLD_WHITE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS_COLOR_CHANNEL_RED").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS_COLOR_CHANNEL_GREEN").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "STATUS_COLOR_CHANNEL_BLUE").toString()
                    });
                    device.setActuatorEndpoints(new String[]{
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "DIM").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "ON").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "OFF").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR_CHANNEL_WARM_WHITE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR_CHANNEL_COLD_WHITE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR_CHANNEL_RED").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR_CHANNEL_GREEN").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR_CHANNEL_BLUE").toString(),
                        UrlUtil.url("zwave", nodeId).addParameter("serialPort", serialPort).addParameter("command", "SET_COLOR").toString()
                    });
                }

            } else {
                LOG.debug("Not a root device: " + device);
            }
            result.add(device);
        }

        return result.toArray(new Device[result.size()]);
    }
}