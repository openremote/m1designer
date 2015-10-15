package org.openremote.test;

import gumi.builders.UrlBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.server.testdata.SampleEnvironmentWidget;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.inventory.ClientPreset;
import org.testng.annotations.Test;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class InventoryServiceTest extends IntegrationTest {

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void getPresets() throws Exception {
        ClientPreset[] presets = fromJson(
            template.requestBody(restClientUrl("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );
        assertEquals(presets.length, 2);
        assertEquals(presets[1].getName(), "Nexus 5");
        assertEquals(presets[1].getAgentLike(), "Nexus 5");
        assertEquals(presets[1].getMinWidth(), 0);
        assertEquals(presets[1].getMinHeight(), 0);
    }

    @Test
    public void addMissingName() throws Exception {
        ClientPreset newPreset = new ClientPreset();

        Exchange postPresetExchange = producerTemplate.request(
            restClientUrl("inventory", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(newPreset));
            }
        );
        assertEquals(postPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 400);
    }

    @Test
    public void addNonUniqueName() throws Exception {
        ClientPreset newPreset = new ClientPreset();
        newPreset.setName("Nexus 5");

        Exchange postPresetExchange = producerTemplate.request(
            restClientUrl("inventory", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(newPreset));
            }
        );
        assertEquals(postPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 409);
    }

    @Test
    public void addDeletePreset() throws Exception {
        ClientPreset newPreset = new ClientPreset();
        newPreset.setName("Test Name");
        newPreset.setAgentLike("Test AgentLike");
        newPreset.setMinHeight(1);
        newPreset.setMaxHeight(2);
        newPreset.setMinWidth(3);
        newPreset.setMaxWidth(4);
        newPreset.setInitialFlowId("123");

        Exchange postPresetExchange = producerTemplate.request(
            restClientUrl("inventory", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(newPreset));
            }
        );
        assertEquals(postPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 201);
        String location = postPresetExchange.getOut().getHeader("Location", String.class);

        ClientPreset preset = fromJson(
            template.requestBody(location, null, String.class),
            ClientPreset.class
        );

        assertEquals(preset.getName(), "Test Name");
        assertEquals(preset.getName(), "Test Name");
        assertEquals(preset.getAgentLike(), "Test AgentLike");
        assertEquals(preset.getMinHeight(), 1);
        assertEquals(preset.getMaxHeight(), 2);
        assertEquals(preset.getMinWidth(), 3);
        assertEquals(preset.getMaxWidth(), 4);
        assertEquals(preset.getInitialFlowId(), "123");

        Exchange deletePresetExchange = producerTemplate.request(
            location,
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.DELETE);
            }
        );
        assertEquals(deletePresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);

        Exchange getPresetExchange = producerTemplate.request(
            UrlBuilder.fromString(location).addParameter("throwExceptionOnFailure", "false").toString(),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
            }
        );
        assertEquals(getPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 404);
    }

    @Test
    public void updatePreset() throws Exception {

        Exchange flowPresetExchange = producerTemplate.request(
            restClientUrl("flow", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
                exchange.getIn().setHeader("agent", "iPad");
                exchange.getIn().setHeader("width", "1024");
                exchange.getIn().setHeader("height", "768");
            }
        );
        assertEquals(flowPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 404);

        final ClientPreset[] presets = fromJson(
            template.requestBody(restClientUrl("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );

        presets[0].setInitialFlowId(SampleEnvironmentWidget.FLOW.getId());

        Exchange putFlowExchange = producerTemplate.request(
            restClientUrl("inventory", "preset", presets[0].getId().toString()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.PUT);
                exchange.getIn().setBody(toJson(presets[0]));
            }
        );
        assertEquals(putFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);

        ClientPreset[] result = fromJson(
            template.requestBody(restClientUrl("inventory", "preset"), null, String.class),
            ClientPreset[].class
        );
        assertEquals(result.length, 2);
        assertEquals(result[0].getName(), "iPad Landscape");
        assertEquals(result[0].getInitialFlowId(), SampleEnvironmentWidget.FLOW.getId());

        flowPresetExchange = producerTemplate.request(
            restClientUrl("flow", "preset"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
                exchange.getIn().setHeader("agent", "ipAD");
                exchange.getIn().setHeader("width", "1024");
                exchange.getIn().setHeader("height", "768");
            }
        );
        assertEquals(flowPresetExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);

        Flow flow = fromJson(flowPresetExchange.getOut().getBody(String.class), Flow.class);
        assertEquals(flow.getId(), SampleEnvironmentWidget.FLOW.getId());
    }

}
