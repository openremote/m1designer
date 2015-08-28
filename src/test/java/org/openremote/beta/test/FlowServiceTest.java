package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.server.util.JsonUtil.JSON;

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
        assertEquals(flow.getIdentifier().getType(), Flow.TYPE);

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

    @Test
    public void createSubflowNode() throws Exception {
        Node subflowNode = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId(), "subflow"), null, String.class),
            Node.class
        );
        assertNotNull(subflowNode.getId());
        assertEquals(subflowNode.getLabel(), SampleTemperatureProcessor.FLOW.getLabel());
        assertEquals(subflowNode.getIdentifier().getType(), Node.TYPE_SUBFLOW);
        assertEquals(subflowNode.getEditorSettings().getTypeLabel(), Node.TYPE_SUBFLOW_LABEL);
        assertEquals(subflowNode.getEditorSettings().getComponents(), new String[] {SubflowRoute.EDITOR_COMPONENT});
        assertEquals(subflowNode.getEditorSettings().getNodeColor(), NodeColor.VIRTUAL);

        assertEquals(subflowNode.getSlots().length, 3);
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SINK).length, 1);
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE).length, 2);
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SINK).length, 1);
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SINK)[0].getLabel(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getLabel());
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SINK)[0].getPeerId(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK.getId());
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE).length, 2);
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE)[0].getLabel(), SampleTemperatureProcessor.CELCIUS_PRODUCER.getLabel());
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE)[0].getPeerId(), SampleTemperatureProcessor.CELCIUS_PRODUCER_SOURCE.getId());
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE)[1].getLabel(), SampleTemperatureProcessor.LABEL_PRODUCER.getLabel());
        assertEquals(subflowNode.findConnectableSlots(Slot.TYPE_SOURCE)[1].getPeerId(), SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE.getId());
    }

    @Test
    public void resolveDependencies() throws Exception {
        Flow flow = new Flow("Test Flow", new Identifier(IdentifierUtil.generateGlobalUniqueId(), Flow.TYPE));

        Node subflowNode = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId(), "subflow"), null, String.class),
            Node.class
        );

        flow.addNode(subflowNode);

        Exchange resolveFlowExchange = producerTemplate.request(
            createWebClientUri("flow", "resolve"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(resolveFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);
        Flow resolvedFlow = fromJson(resolveFlowExchange.getOut().getBody(String.class), Flow.class);

        assertEquals(resolvedFlow.getDependencies().length, 1);
        assertEquals(resolvedFlow.getDependencies()[0].getId(), SampleTemperatureProcessor.FLOW.getId());
    }

}
