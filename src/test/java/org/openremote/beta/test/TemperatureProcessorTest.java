package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.openremote.beta.shared.event.FlowStopEvent;
import org.openremote.beta.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

public class TemperatureProcessorTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:eventReceiver")
    MockEndpoint eventReceiver;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:sendEvent")
                    .to(createWebSocketUri("events"));

                from(createWebSocketUri("events"))
                    .to("log:EVENT_RECEIVED: ${body}")
                    .to("mock:eventReceiver");
            }
        };
    }

    @Test
    public void execute() throws Exception {

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockTemperatureDatabase = context().getEndpoint("mock:temperatureDatabase", MockEndpoint.class);
        MockEndpoint mockProducerCelcius = context().getEndpoint("mock:producerCelcius", MockEndpoint.class);
        MockEndpoint mockProducerLabel = context().getEndpoint("mock:producerLabel", MockEndpoint.class);

        mockTemperatureDatabase.expectedBodiesReceived(75);
        mockProducerCelcius.expectedBodiesReceived("24");
        mockProducerLabel.expectedBodiesReceived("24 \u00B0C");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                "75"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                "24"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                "24 \u00B0C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK, "75"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockTemperatureDatabase.assertIsSatisfied();
        mockProducerCelcius.assertIsSatisfied();
        mockProducerLabel.assertIsSatisfied();

        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STOPPING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STOPPED))
        );
        FlowStopEvent flowStopEvent = new FlowStopEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody("direct:sendEvent", flowStopEvent);
        eventReceiver.assertIsSatisfied();
    }

}
