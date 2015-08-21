package org.openremote.beta.test;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.flow.FlowService;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.devicediscovery.domain.DiscoveredDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.server.util.JsonUtil.JSON;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.STARTING;

public class FlowServiceTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlowServiceTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void getFlows() throws Exception {
        Flow[] flows = fromJson(
            template.requestBody(createWebClientUri("flow"), null, String.class),
            Flow[].class
        );
        assertEquals(flows.length, 3);
        assertEquals(flows[0].getId(), SampleEnvironmentWidget.FLOW.getId());
        assertEquals(flows[0].getLabel(), SampleEnvironmentWidget.FLOW.getLabel());
        assertEquals(flows[0].getNodes().length, 0);
        assertEquals(flows[0].getWires().length, 0);
        assertEquals(flows[0].getDependencies().length, 0);
        assertEquals(flows[1].getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(flows[1].getLabel(), SampleThermostatControl.FLOW.getLabel());
        assertEquals(flows[1].getNodes().length, 0);
        assertEquals(flows[1].getWires().length, 0);
        assertEquals(flows[1].getDependencies().length, 0);
        assertEquals(flows[2].getId(), SampleTemperatureProcessor.FLOW.getId());
        assertEquals(flows[2].getLabel(), SampleTemperatureProcessor.FLOW.getLabel());
        assertEquals(flows[2].getNodes().length, 0);
        assertEquals(flows[2].getWires().length, 0);
        assertEquals(flows[2].getDependencies().length, 0);
    }

    @Test
    public void createDeleteFlow() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", "template"), null, String.class),
            Flow.class
        );
        assertNotNull(flow.getId());
        assertEquals(flow.getLabel(), "My Flow");

        flow.setLabel("TEST LABEL");

        final String flowJson = JSON.writeValueAsString(flow);
        Exchange postFlowExchange = producerTemplate.request(
            createWebClientUri("flow"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(flowJson);
            }
        );
        assertEquals(postFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 201);

        flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", flow.getId()), null, String.class),
            Flow.class
        );
        assertNotNull(flow.getId());
        assertEquals(flow.getLabel(), "TEST LABEL");
        assertEquals(flow.getNodes().length, 0);
        assertEquals(flow.getWires().length, 0);
        assertEquals(flow.getDependencies().length, 0);

        Exchange deleteFlowExchange = producerTemplate.request(
            createWebClientUri("flow", flow.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.DELETE);
            }
        );
        assertEquals(deleteFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);

        Exchange getFlowExchange = producerTemplate.request(
            createWebClientUri("flow", flow.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.GET);
            }
        );
        assertEquals(getFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 404);
    }

    @Test
    public void deleteUsedFlow() {
        Exchange deleteFlowExchange = producerTemplate.request(
            createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.DELETE);
            }
        );
        assertEquals(deleteFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 409); // Conflict
    }

    @Test
    public void readWriteFlow() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.getId(), SampleTemperatureProcessor.FLOW.getId());
        assertEquals(flow.getLabel(), SampleTemperatureProcessor.FLOW.getLabel());
        assertEquals(flow.getNodes().length, 6);
        assertEquals(flow.getWires().length, 5);
        assertEquals(flow.getDependencies().length, 0);

        flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(flow.getLabel(), SampleThermostatControl.FLOW.getLabel());
        assertEquals(flow.getNodes().length, 13);
        assertEquals(flow.getWires().length, 12);
        assertEquals(flow.getDependencies().length, 1);
        assertEquals(flow.getDependencies()[0].getId(), SampleTemperatureProcessor.FLOW.getId());

        final Flow updateFlow = flow;
        updateFlow.setLabel("New Label");

        // We except that the client will NOT calculate dependencies and put an empty array
        updateFlow.clearDependencies();

        Exchange putFlowExchange = producerTemplate.request(
            createWebClientUri("flow", flow.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.PUT);
                exchange.getIn().setBody(toJson(updateFlow));
            }
        );
        assertEquals(putFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);

        flow = fromJson(
            template.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(flow.getLabel(), "New Label");
        assertEquals(flow.getNodes().length, 13);
        assertEquals(flow.getWires().length, 12);
        assertEquals(flow.getDependencies().length, 1);
        assertEquals(flow.getDependencies()[0].getId(), SampleTemperatureProcessor.FLOW.getId());
    }
}
