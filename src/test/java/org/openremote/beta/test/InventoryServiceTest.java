package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.inventory.ClientPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class InventoryServiceTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void getPresets() throws Exception {
        ClientPreset[] presets = fromJson(
            template.requestBody(createWebClientUri("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );
        assertEquals(presets.length, 2);
        assertEquals(presets[0].getName(), "iPad Landscape");
        assertEquals(presets[0].getMinWidth(), 1024);
        assertEquals(presets[0].getMinHeight(), 768);
    }

    @Test
    public void updatePreset() throws Exception {

        Exchange flowPresetExchange = producerTemplate.request(
            createWebClientUri("flow", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
                exchange.getIn().setHeader("agent", "iPad");
                exchange.getIn().setHeader("width", "1024");
                exchange.getIn().setHeader("height", "768");
            }
        );
        assertEquals(flowPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 404);

        final ClientPreset[] presets = fromJson(
            template.requestBody(createWebClientUri("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );

        presets[0].setInitialFlowId(SampleEnvironmentWidget.FLOW.getId());

        Exchange putFlowExchange = producerTemplate.request(
            createWebClientUri("inventory", "preset", presets[0].getName()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.PUT);
                exchange.getIn().setBody(toJson(presets[0]));
            }
        );
        assertEquals(putFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);

        ClientPreset[] result = fromJson(
            template.requestBody(createWebClientUri("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );
        assertEquals(result.length, 2);
        assertEquals(result[0].getName(), "iPad Landscape");
        assertEquals(result[0].getInitialFlowId(), SampleEnvironmentWidget.FLOW.getId());

        flowPresetExchange = producerTemplate.request(
            createWebClientUri("flow", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
                exchange.getIn().setHeader("agent", "iPad");
                exchange.getIn().setHeader("width", "1024");
                exchange.getIn().setHeader("height", "768");
            }
        );
        assertEquals(flowPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);

        Flow flow = fromJson(flowPresetExchange.getOut().getBody(String.class), Flow.class);
        assertEquals(flow.getId(), SampleEnvironmentWidget.FLOW.getId());
    }


}
