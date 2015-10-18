package org.openremote.test;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.server.Configuration;
import org.openremote.server.inventory.discovery.AdapterDiscoveryService;
import org.openremote.server.inventory.discovery.DiscoveryServiceConfiguration;
import org.openremote.shared.inventory.Adapter;
import org.openremote.shared.model.PropertyDescriptor;
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

        context.addService(new AdapterDiscoveryService(context));
    }

    @Test
    public void discovery() throws Exception {

        // Check the inbox, it should be empty
        DiscoveredDeviceDTO[] discoveredDevices = fromJson(
            template.requestBody(restClientUrl("discovery", "inbox"), null, String.class),
            DiscoveredDeviceDTO[].class
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
        assertEquals(adapter.getProperties().get("serialPort").getDescriptor().getLabel(), "Serial Port");
        assertEquals(adapter.getProperties().get("serialPort").getDescriptor().getClass(), PropertyDescriptor.TYPE_STRING.getClass());
        assertTrue(adapter.getProperties().get("serialPort").getDescriptor().isRequired());
        assertEquals(adapter.getProperties().get("command").getDescriptor().getLabel(), "Command");
        assertEquals(adapter.getProperties().get("command").getDescriptor().getClass(), PropertyDescriptor.TYPE_STRING.getClass());
        assertFalse(adapter.getProperties().get("command").getDescriptor().isRequired());
        assertEquals(adapter.getProperties().get("arg").getDescriptor().getLabel(), "Argument");
        assertEquals(adapter.getProperties().get("arg").getDescriptor().getClass(), PropertyDescriptor.TYPE_STRING.getClass());
        assertFalse(adapter.getProperties().get("arg").getDescriptor().isRequired());

        // Now configure the adapter and add it to the inbox for auto-discovery

        adapter.getProperties().get("serialPort").setValue("TEST_SERIAL_PORT");

        Exchange addAdapterToInboxExchange = createExchangeWithBody(context(), toJson(adapter));
        addAdapterToInboxExchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
        template.send(
            restClientUrl("discovery", "inbox", "adapter"),
            addAdapterToInboxExchange
        );
        String adapterLocation = addAdapterToInboxExchange.getOut().getHeader("Location", String.class);

        // Wait a bit...
        Thread.sleep(1000);

        // Trigger refresh of inbox
        discoveredDevices = fromJson(
            template.requestBodyAndHeader(
                restClientUrl("discovery", "inbox"),
                null,
                "refresh", "true",
                String.class
            ),
            DiscoveredDeviceDTO[].class
        );
        // TODO: We don't have stable IDs yet...
        assertTrue(discoveredDevices.length > 0);

        // Wait a bit...
        Thread.sleep(1000);

        discoveredDevices = fromJson(
            template.requestBody(restClientUrl("discovery", "inbox"), null, String.class),
            DiscoveredDeviceDTO[].class
        );
        // TODO: We don't have stable IDs yet...
        assertTrue(discoveredDevices.length > 0);

        assertEquals(discoveredDevices[0].getName(), mockDeviceDTO.getName());
        assertEquals(discoveredDevices[0].getDeviceAttrs().size(), mockDeviceAttrDTO.size());

        // Now remove the adapter from the inbox (disabling discovyer)

        Adapter[] inboxAdapters = fromJson(
            template.requestBody(restClientUrl("discovery", "inbox", "adapter"), null, String.class),
            Adapter[].class
        );
        assertEquals(inboxAdapters.length, 1);

        Exchange removeAdapterFromInboxExchange = createExchangeWithBody(context(), null);
        removeAdapterFromInboxExchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.DELETE);
        template.send(
            adapterLocation,
            removeAdapterFromInboxExchange
        );
        assertEquals(removeAdapterFromInboxExchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE), 200);

        inboxAdapters = fromJson(
            template.requestBody(restClientUrl("discovery", "inbox", "adapter"), null, String.class),
            Adapter[].class
        );
        assertEquals(inboxAdapters.length, 0);
    }

}
