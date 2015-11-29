package org.openremote.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.beta.zwave.component.MockZWAdapter;
import org.openremote.beta.zwave.component.ZWAdapter;
import org.openremote.beta.zwave.component.ZWComponent;
import org.openremote.devicediscovery.domain.DiscoveredDeviceAttrDTO;
import org.openremote.devicediscovery.domain.DiscoveredDeviceDTO;
import org.openremote.server.Configuration;
import org.openremote.server.inventory.discovery.DiscoveryService;
import org.openremote.server.inventory.discovery.DiscoveryServiceConfiguration;
import org.openremote.shared.inventory.Adapter;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.openremote.protocol.zwave.model.commandclasses.DeviceDiscoveryCommandClassVisitor.*;

public class DiscoveryTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryTest.class);

    MockZWAdapter mockZWAdapter;
    DiscoveredDeviceDTO mockDeviceDTO = new DiscoveredDeviceDTO();
    List<DiscoveredDeviceAttrDTO> mockDeviceAttrDTO = new ArrayList<>();

    @Override
    protected void configure(java.util.Properties properties, List<Configuration> configurations, CamelContext context) throws Exception {
        super.configure(properties, configurations, context);

        configurations.add(new DiscoveryServiceConfiguration());

        context.addComponent("zwaveMock", new ZWComponent(new ZWAdapter.Manager() {
            @Override
            public ZWAdapter openAdapter(String serialPort) {
                if ("TEST_SERIAL_PORT".equals(serialPort))
                    return mockZWAdapter;
                throw new IllegalArgumentException("Unsupported serial port: " + serialPort);
            }

            @Override
            public void closeAdapter(ZWAdapter adapter) {
                assertTrue(adapter.equals(mockZWAdapter));

            }
        }));

        mockZWAdapter = new MockZWAdapter("TEST_SERIAL_PORT") {
            @Override
            protected String[] getMockInitialState(MockCommand mockCommand) {
                return new String[]{"0"};
            }

            @Override
            protected String updateMockState(MockCommand mockCommand) {
                if (mockCommand.command.equals("OFF")) {
                    return "0";
                }
                return super.updateMockState(mockCommand);
            }
        };

        mockDeviceDTO.setName("Mock Device");
        mockDeviceDTO.setDeviceAttrs(mockDeviceAttrDTO);

        DiscoveredDeviceAttrDTO attribute = new DiscoveredDeviceAttrDTO();
        attribute.setName(ATTR_NAME_NODE_ID);
        attribute.setValue("123");
        mockDeviceAttrDTO.add(attribute);

        attribute = new DiscoveredDeviceAttrDTO();
        attribute.setName(ATTR_NAME_DEVICE_DISCOVERY_COMMAND);
        attribute.setValue("add");
        mockDeviceAttrDTO.add(attribute);

        attribute = new DiscoveredDeviceAttrDTO();
        attribute.setName(ATTR_NAME_CONTROLLER_COMMANDS);
        attribute.setValue("STATUS OFF DIM");
        mockDeviceAttrDTO.add(attribute);

        mockZWAdapter.discoveredDeviceDTOs.add(mockDeviceDTO);

        context.addService(new DiscoveryService(context));
    }

    @Test
    public void discovery() throws Exception {

        // Check the discovered devices, it should be empty
        Device[] discoveredDevices = fromJson(
            template.requestBody(restClientUrl("discovery", "device"), null, String.class),
            Device[].class
        );
        assertEquals(discoveredDevices.length, 0);

        // Find the available adapters and their configuration metadata

        Adapter[] adapters = fromJson(
            template.requestBody(restClientUrl("discovery", "adapter"), null, String.class),
            Adapter[].class
        );
        assertTrue(adapters.length > 0);

        Adapter adapter = fromJson(
            template.requestBody(restClientUrl("discovery", "adapter", "zwaveMock"), null, String.class),
            Adapter.class
        );

        assertEquals(adapter.getId(), "zwaveMock");
        assertEquals(adapter.getDiscoveryEndpoint(), "zwaveMock://discovery");
        assertEquals(adapter.getLabel(), "ZWave Mock Adapter");
        ObjectNode properties = fromJson(adapter.getProperties(), ObjectNode.class);
        assertEquals(properties.size(), 3);
        assertEquals(properties.get("serialPort").get("label").asText(), "Serial Port");
        assertEquals(properties.get("serialPort").get("type").asText(), "string");
        assertTrue(properties.get("serialPort").get("required").asBoolean());
        assertEquals(properties.get("command").get("label").asText(), "Command");
        assertEquals(properties.get("command").get("type").asText(), "string");
        assertFalse(properties.get("command").has("required"));
        assertEquals(properties.get("arg").get("label").asText(), "Argument");
        assertEquals(properties.get("arg").get("type").asText(), "string");
        assertFalse(properties.get("arg").has("required"));

        // Now configure the adapter and add it to the inbox for auto-discovery

        properties.with("serialPort").put("value", "TEST_SERIAL_PORT");
        adapter.setProperties(toJson(properties));

        Exchange triggerDiscoveryExchange = createExchangeWithBody(context(), toJson(adapter));
        triggerDiscoveryExchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
        template.send(
            restClientUrl("discovery", "adapter", "trigger"),
            triggerDiscoveryExchange
        );
        String devicesLocation = triggerDiscoveryExchange.getOut().getHeader("Location", String.class);

        // Wait a bit...
        Thread.sleep(1000);

        discoveredDevices = fromJson(
            template.requestBody(devicesLocation, null, String.class),
            Device[].class
        );
        assertTrue(discoveredDevices.length > 0);
        assertEquals(discoveredDevices[0].getStatus(), Device.Status.UNINITIALIZED);
        assertEquals(discoveredDevices[0].getLabel(), mockDeviceDTO.getName());
        ObjectNode discoveredProperties = fromJson(discoveredDevices[0].getProperties(), ObjectNode.class);
        assertEquals(discoveredProperties.get(ATTR_NAME_CONTROLLER_COMMANDS).asText(), "STATUS OFF DIM");
    }

}
