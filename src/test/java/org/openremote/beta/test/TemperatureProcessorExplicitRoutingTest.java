package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.beta.server.processor.FlowProcessor;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.shared.util.Util.createMap;

/**
 * TODO: An alternative to Camel routing, we have only one route and handle it all through custom processors in that route
 *
 * This might be an alternative core architecture if Camel is too slow/needs too much memory with thousands of routes
 */
public class TemperatureProcessorExplicitRoutingTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorExplicitRoutingTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    /* ###################################################################################### */

    static Slot FAHRENHEIT_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    static Node FAHRENHEIT_CONSUMER = new Node("Fahrenheit", new Identifier(generateGlobalUniqueId(), Node.TYPE_CONSUMER), FAHRENHEIT_CONSUMER_SOURCE);

    static Slot FAHRENHEIT_CONVERTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    static Slot FAHRENHEIT_CONVERTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    static Node FAHRENHEIT_CONVERTER = new Node("Fahrenheit to Celcius", new Identifier(generateGlobalUniqueId(), Node.TYPE_FUNCTION), FAHRENHEIT_CONVERTER_SINK, FAHRENHEIT_CONVERTER_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        properties.put("javascript", "~~(((parseInt(request.body, 10) - 32)*5)/9)");
        FAHRENHEIT_CONVERTER.setProperties(properties);
    }

    static Slot TEMPERATURE_DATABASE_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    static Node TEMPERATURE_DATABASE = new Node("Temperature Database", new Identifier(generateGlobalUniqueId(), Node.TYPE_STORAGE), TEMPERATURE_DATABASE_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("mockEndpoint", "mock:temperatureDatabase");
        TEMPERATURE_DATABASE.setProperties(properties);
    }

    static Slot CELCIUS_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    static Node CELCIUS_PRODUCER = new Node("Celcius", new Identifier(generateGlobalUniqueId(), Node.TYPE_PRODUCER), CELCIUS_PRODUCER_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("mockEndpoint", "mock:producerCelcius");
        CELCIUS_PRODUCER.setProperties(properties);
    }

    static {
        Map<String, Object> properties = createMap();
        properties.put("mockEndpoint", "mock:producerCelcius");
        CELCIUS_PRODUCER.setProperties(properties);
    }

    static Slot CELCIUS_APPENDER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    static Slot CELCIUS_APPENDER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    static Node CELCIUS_APPENDER = new Node("Append Celcius Symbol", new Identifier(generateGlobalUniqueId(), Node.TYPE_CHANGE), CELCIUS_APPENDER_SINK, CELCIUS_APPENDER_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        properties.put("append", " C");
        CELCIUS_APPENDER.setProperties(properties);
    }

    static Slot LABEL_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    static Node LABEL_PRODUCER = new Node("Label", new Identifier(generateGlobalUniqueId(), Node.TYPE_PRODUCER), LABEL_PRODUCER_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("mockEndpoint", "mock:producerLabel");
        LABEL_PRODUCER.setProperties(properties);
    }

    static Node[] FLOW_NODES = new Node[]{FAHRENHEIT_CONSUMER, FAHRENHEIT_CONVERTER, TEMPERATURE_DATABASE, CELCIUS_PRODUCER, CELCIUS_APPENDER, LABEL_PRODUCER};

    static Flow FLOW = new Flow(
        "Temperature Processor",
        new Identifier(generateGlobalUniqueId(), Flow.TYPE),
        FLOW_NODES,
        new Wire[]{
            new Wire(FAHRENHEIT_CONSUMER_SOURCE, FAHRENHEIT_CONVERTER_SINK),
            new Wire(FAHRENHEIT_CONSUMER_SOURCE, TEMPERATURE_DATABASE_SINK),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE, CELCIUS_PRODUCER_SINK),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE, CELCIUS_APPENDER_SINK),
            new Wire(CELCIUS_APPENDER_SOURCE, LABEL_PRODUCER_SINK)
        }
    );
    /* ###################################################################################### */

    @EndpointInject(uri = "mock:temperatureDatabase")
    MockEndpoint mockTemperatureDatabase;

    @EndpointInject(uri = "mock:producerCelcius")
    MockEndpoint mockProducerCelcius;

    @EndpointInject(uri = "mock:producerLabel")
    MockEndpoint mockProducerLabel;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:" + FLOW.getIdentifier().getId())
                    .id(FLOW.toString())
                    .bean(new FlowProcessor(producerTemplate, FLOW));
            }
        };
    }

    @Test
    public void process() throws Exception {

        mockTemperatureDatabase.reset();
        mockProducerCelcius.reset();
        mockProducerLabel.reset();

        mockTemperatureDatabase.expectedBodiesReceived(75);
        mockProducerCelcius.expectedBodiesReceived("23");
        mockProducerLabel.expectedBodiesReceived("23 C");

        Map<String, Object> headers = new HashMap<>();
        headers.put(FlowProcessor.DESTINATION_NODE_ID, FAHRENHEIT_CONSUMER.getIdentifier().getId());
        headers.put(FlowProcessor.DESTINATION_SINK_ID, null);

        producerTemplate.sendBodyAndHeaders(
            "direct:" + FLOW.getIdentifier().getId(), 75, headers
        );

        mockTemperatureDatabase.assertIsSatisfied();
        mockProducerCelcius.assertIsSatisfied();
        mockProducerLabel.assertIsSatisfied();

    }

}
