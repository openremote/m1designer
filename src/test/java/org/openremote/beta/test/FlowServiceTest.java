package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpMethods;
import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.flow.*;
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
        assertEquals(flows[0].getSuperDependencies().length, 0);
        assertEquals(flows[0].getSubDependencies().length, 0);
        assertEquals(flows[1].getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(flows[1].getLabel(), SampleThermostatControl.FLOW.getLabel());
        assertEquals(flows[1].getNodes().length, 0);
        assertEquals(flows[1].getWires().length, 0);
        assertEquals(flows[2].getId(), SampleTemperatureProcessor.FLOW.getId());
        assertEquals(flows[2].getLabel(), SampleTemperatureProcessor.FLOW.getLabel());
        assertEquals(flows[2].getNodes().length, 0);
        assertEquals(flows[2].getWires().length, 0);
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
        assertEquals(flow.getSuperDependencies().length, 0);
        assertEquals(flow.getSubDependencies().length, 0);

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
    public void readWriteFlow() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.getId(), SampleTemperatureProcessor.FLOW.getId());
        assertEquals(flow.getLabel(), SampleTemperatureProcessor.FLOW.getLabel());
        assertEquals(flow.getNodes().length, 6);
        assertEquals(flow.getWires().length, 5);
        assertEquals(flow.getSuperDependencies().length, 2);
        assertEquals(flow.getSubDependencies().length, 0);

        flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.getId(), SampleThermostatControl.FLOW.getId());
        assertEquals(flow.getLabel(), SampleThermostatControl.FLOW.getLabel());
        assertEquals(flow.getNodes().length, 13);
        assertEquals(flow.getWires().length, 12);
        assertEquals(flow.getSuperDependencies().length, 1);
        assertEquals(flow.getSuperDependencies()[0].getId(), SampleEnvironmentWidget.FLOW.getId());
        assertEquals(flow.getSubDependencies().length, 1);
        assertEquals(flow.getSubDependencies()[0].getId(), SampleTemperatureProcessor.FLOW.getId());

        final Flow updateFlow = flow;
        updateFlow.setLabel("New Label");

        // We except that the client will NOT calculate dependencies
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
        assertEquals(flow.getSuperDependencies().length, 1);
        assertEquals(flow.getSuperDependencies()[0].getId(), SampleEnvironmentWidget.FLOW.getId());
        assertEquals(flow.getSubDependencies().length, 1);
        assertEquals(flow.getSubDependencies()[0].getId(), SampleTemperatureProcessor.FLOW.getId());

        // The label of the subflow nodes in the super flow should be unchanged
        flow = fromJson(
            template.requestBody(createWebClientUri("flow", SampleEnvironmentWidget.FLOW.getId()), null, String.class),
            Flow.class
        );
        assertEquals(flow.findNode(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId()).getLabel(), SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getLabel());
        assertEquals(flow.findNode(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getId()).getLabel(), SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getLabel());

        // All wires should still be present
        assertEquals(flow.findWiresAttachedToNode(flow.findNode(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId())).length, 3);
        assertEquals(flow.findWiresAttachedToNode(flow.findNode(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getId())).length, 3);
    }

    @Test
    public void duplicateNode() throws Exception {
        Node node = SampleTemperatureProcessor.FAHRENHEIT_CONVERTER;

        Exchange duplicateNodeExchange = producerTemplate.request(
            createWebClientUri("flow", "duplicate", "node"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(node));
            }
        );
        assertEquals(duplicateNodeExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);
        Node dupe = fromJson(duplicateNodeExchange.getOut().getBody(String.class), Node.class);

        assertNotEquals(node.getId(), dupe.getId());
        assertNotEquals(node.getLabel(), dupe.getLabel());

        assertEquals(node.getEditorSettings().getNodeColor(), dupe.getEditorSettings().getNodeColor());
        assertEquals(node.getEditorSettings().getTypeLabel(), dupe.getEditorSettings().getTypeLabel());

        for (int i = 0; i < node.getSlots().length; i++) {
            assertNotEquals(node.getSlots()[i].getId(), dupe.getSlots()[i].getId());
            assertEquals(node.getSlots()[i].getPeerId(), dupe.getSlots()[i].getPeerId());
            assertEquals(node.getSlots()[i].getPropertyPath(), dupe.getSlots()[i].getPropertyPath());
        }
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
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId(), "subflow"), null, String.class),
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

        assertEquals(resolvedFlow.getSuperDependencies().length, 0);
        assertEquals(resolvedFlow.getSubDependencies().length, 2);
        assertEquals(resolvedFlow.getSubDependencies()[0].getId(), SampleThermostatControl.FLOW.getId());
        assertNull(resolvedFlow.getSubDependencies()[0].getFlow());
        assertEquals(resolvedFlow.getSubDependencies()[1].getId(), SampleTemperatureProcessor.FLOW.getId());
        assertNull(resolvedFlow.getSubDependencies()[1].getFlow());

        // Again but hydrate sub dependencies
        resolveFlowExchange = producerTemplate.request(
            createWebClientUri("flow", "resolve"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setHeader("hydrateSubs", true);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(resolveFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);
        resolvedFlow = fromJson(resolveFlowExchange.getOut().getBody(String.class), Flow.class);

        assertEquals(resolvedFlow.getSubDependencies()[0].getFlow().getIdentifier(), SampleThermostatControl.FLOW.getIdentifier());
        assertEquals(resolvedFlow.getSubDependencies()[1].getFlow().getIdentifier(), SampleTemperatureProcessor.FLOW.getIdentifier());

        // Now attach a wire to test hard super-dependencies

        Node textLabelNode = fromJson(
            producerTemplate.requestBody(createWebClientUri("catalog", "node", TextLabelNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        flow.addNode(textLabelNode);
        Slot textSink = textLabelNode.getSlots()[0];
        Slot setpointSource = subflowNode.findSlots(Slot.TYPE_SOURCE)[0];
        flow.addWireBetweenSlots(setpointSource, textSink);

        // Must save this flow first
        Exchange postFlowExchange = producerTemplate.request(
            createWebClientUri("flow"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(postFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 201);

        // This guy should now have a new hard super-dependency
        Flow sampleThermostatControl = SampleThermostatControl.getCopy();
        resolveFlowExchange = producerTemplate.request(
            createWebClientUri("flow", "resolve"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(sampleThermostatControl));
            }
        );
        assertEquals(resolveFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 200);
        resolvedFlow = fromJson(resolveFlowExchange.getOut().getBody(String.class), Flow.class);

        assertEquals(resolvedFlow.getSuperDependencies().length, 2);
        assertTrue(resolvedFlow.getSuperDependencies()[0].isWired());
        assertFalse(resolvedFlow.getSuperDependencies()[0].isPeersInvalid());
        assertTrue(resolvedFlow.getSuperDependencies()[1].isWired());
        assertFalse(resolvedFlow.getSuperDependencies()[1].isPeersInvalid());

        assertEquals(resolvedFlow.getDirectSuperDependencies().length, 2);
    }

    @Test
    public void readWriteDependenciesRemoveFlow() throws Exception {
        Exchange deleteFlowExchange = producerTemplate.request(
            createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.DELETE);
            }
        );

        assertEquals(deleteFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);

        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );

        Node temperatureConsumer = flow.findNode(SampleThermostatControl.TEMPERATURE_CONSUMER.getId());
        Node temperatureSubflowNode = flow.findNode(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW.getId());
        Node temperatureLabel = flow.findNode(SampleThermostatControl.TEMPERATURE_LABEL.getId());
        assertNull(temperatureSubflowNode);
        assertEquals(flow.findWiresAttachedToNode(temperatureConsumer).length, 0);
        assertEquals(flow.findWiresAttachedToNode(temperatureLabel).length, 0);
    }

    @Test
    public void readWriteDependenciesRemoveConsumer() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()), null, String.class),
            Flow.class
        );

        final Flow updateFlow = flow;

        // This is a node others depend on
        Node fahrenheitConsumer = updateFlow.findNode(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getId());
        updateFlow.removeNode(fahrenheitConsumer);

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
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );

        Node temperatureConsumer = flow.findNode(SampleThermostatControl.TEMPERATURE_CONSUMER.getId());
        Node temperatureSubflowNode = flow.findNode(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW.getId());
        Node temperatureLabel = flow.findNode(SampleThermostatControl.TEMPERATURE_LABEL.getId());
        assertEquals(temperatureSubflowNode.getSlots().length, 2);
        assertEquals(flow.findWiresAttachedToNode(temperatureSubflowNode).length, 1);
        assertEquals(flow.findWiresBetween(temperatureConsumer, temperatureSubflowNode).length, 0);
        assertEquals(flow.findWiresBetween(temperatureSubflowNode, temperatureLabel).length, 1);
    }

    @Test
    public void readWriteDependenciesRenameConsumer() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()), null, String.class),
            Flow.class
        );

        final Flow updateFlow = flow;

        // This is a node others depend on
        Node fahrenheitConsumer = updateFlow.findNode(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getId());
        fahrenheitConsumer.setLabel("New Consumer Name");

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
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );

        Node temperatureConsumer = flow.findNode(SampleThermostatControl.TEMPERATURE_CONSUMER.getId());
        Node temperatureSubflowNode = flow.findNode(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW.getId());
        Node temperatureLabel = flow.findNode(SampleThermostatControl.TEMPERATURE_LABEL.getId());
        assertEquals(temperatureSubflowNode.findSlot(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK.getId()).getLabel(), "New Consumer Name");
        assertEquals(temperatureSubflowNode.getSlots().length, 3);
        assertEquals(flow.findWiresAttachedToNode(temperatureSubflowNode).length, 2);
        assertEquals(flow.findWiresBetween(temperatureConsumer, temperatureSubflowNode).length, 1);
        assertEquals(flow.findWiresBetween(temperatureSubflowNode, temperatureLabel).length, 1);
    }

    @Test
    public void readWriteDependenciesAddConsumer() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(createWebClientUri("flow", SampleTemperatureProcessor.FLOW.getId()), null, String.class),
            Flow.class
        );

        final Flow updateFlow = flow;

        Node newConsumer = fromJson(
            producerTemplate.requestBody(createWebClientUri("catalog", "node", Node.TYPE_CONSUMER), null, String.class),
            Node.class
        );
        newConsumer.setLabel("New Consumer");
        updateFlow.addNode(newConsumer);

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
            producerTemplate.requestBody(createWebClientUri("flow", SampleThermostatControl.FLOW.getId()), null, String.class),
            Flow.class
        );

        Node temperatureConsumer = flow.findNode(SampleThermostatControl.TEMPERATURE_CONSUMER.getId());
        Node temperatureSubflowNode = flow.findNode(SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW.getId());
        Node temperatureLabel = flow.findNode(SampleThermostatControl.TEMPERATURE_LABEL.getId());
        assertEquals(temperatureSubflowNode.findSlotWithPeer(newConsumer.findSlots(Slot.TYPE_SINK)[0].getId()).getLabel(), "New Consumer");
        assertEquals(temperatureSubflowNode.getSlots().length, 4);
        assertEquals(flow.findWiresAttachedToNode(temperatureSubflowNode).length, 2);
        assertEquals(flow.findWiresBetween(temperatureConsumer, temperatureSubflowNode).length, 1);
        assertEquals(flow.findWiresBetween(temperatureSubflowNode, temperatureLabel).length, 1);
    }
}
