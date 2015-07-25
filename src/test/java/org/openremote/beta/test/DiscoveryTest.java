package org.openremote.beta.test;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.inventory.InventoryServiceConfiguration;
import org.openremote.beta.server.inventory.discovery.AdapterDiscoveryService;
import org.openremote.beta.shared.inventory.Adapter;
import org.openremote.beta.shared.inventory.Property;
import org.openremote.beta.zwave.component.MockZWAdapter;
import org.openremote.beta.zwave.component.ZWAdapter;
import org.openremote.beta.zwave.component.ZWComponent;
import org.openremote.devicediscovery.domain.DiscoveredDeviceAttrDTO;
import org.openremote.devicediscovery.domain.DiscoveredDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.openremote.controller.protocol.zwave.model.commandclasses.DeviceDiscoveryCommandClassVisitor.*;

public class DiscoveryTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryTest.class);

    MockZWAdapter mockZWAdapter;
    DiscoveredDeviceDTO mockDeviceDTO = new DiscoveredDeviceDTO();
    List<DiscoveredDeviceAttrDTO> mockDeviceAttrDTO = new ArrayList<>();

    @Override
    protected void configure(Properties properties, List<Configuration> configurations, CamelContext context) throws Exception {
        super.configure(properties, configurations, context);

        configurations.add(new InventoryServiceConfiguration());

        context.addComponent("zwaveMock", new ZWComponent(new ZWAdapter.Manager() {
            @Override
            public ZWAdapter openAdapter(String serialPort) {
                return mockZWAdapter;
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

        context.addService(new AdapterDiscoveryService(context));
    }

    @Test
    public void getAdapters() throws Exception {
        Exchange request = createExchangeWithBody(null);
        template.send(createHttpUri("adapter", "zwaveMock"), request);
        Adapter adapter = readResponse(request, Adapter.class);
        assertEquals(adapter.getIdentifier().getId(), "zwaveMock");
        assertEquals(adapter.getDiscoveryEndpoint(), "zwaveMock://discovery");
        assertEquals(adapter.getLabel(), "ZWave Mock Adapter");
        assertEquals( adapter.getExtra().get("arg").getLabel(), "Argument");
        assertEquals( adapter.getExtra().get("arg").getType(), Property.Type.STRING);
        assertEquals( adapter.getExtra().get("command").getLabel(), "Command");
        assertEquals( adapter.getExtra().get("command").getType(), Property.Type.STRING);
        assertEquals( adapter.getExtra().get("serialPort").getLabel(), "Serial Port");
        assertEquals( adapter.getExtra().get("serialPort").getType(), Property.Type.STRING);
    }

}
