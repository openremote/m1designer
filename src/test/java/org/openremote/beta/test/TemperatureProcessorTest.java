package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TemperatureProcessorTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:flowEventReceiver")
    MockEndpoint flowEventReceiver;

    @EndpointInject(uri = "mock:messageEventReceiver")
    MockEndpoint messageEventReceiver;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:sendFlowEvent")
                    .to(createWebSocketUri("flow"));

                from("direct:sendMessageEvent")
                    .to(createWebSocketUri("message"));

                from(createWebSocketUri("flow"))
                    .to("log:FLOW_EVENT_RECEIVED: ${body}")
                    .to("mock:flowEventReceiver");

                from(createWebSocketUri("message"))
                    .to("log:MESSAGE_EVENT_RECEIVED: ${body}")
                    .to("mock:messageEventReceiver");
            }
        };
    }

    @Test
    public void execute() throws Exception {

        flowEventReceiver.reset();
        flowEventReceiver.expectedBodiesReceived(
            toJson(new FlowStartedEvent(SampleTemperatureProcessor.FLOW.getIdentifier().getId()))
        );
        FlowStartEvent flowStartEvent = new FlowStartEvent(SampleTemperatureProcessor.FLOW);
        producerTemplate.sendBody("direct:sendFlowEvent", flowStartEvent);
        flowEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockTemperatureDatabase = context().getEndpoint("mock:temperatureDatabase", MockEndpoint.class);
        MockEndpoint mockProducerCelcius = context().getEndpoint("mock:producerCelcius", MockEndpoint.class);
        MockEndpoint mockProducerLabel = context().getEndpoint("mock:producerLabel", MockEndpoint.class);

        mockTemperatureDatabase.expectedBodiesReceived(75);
        mockProducerCelcius.expectedBodiesReceived("23");
        mockProducerLabel.expectedBodiesReceived("23 C");

        final String INSTANCE_ID = IdentifierUtil.generateGlobalUniqueId();

        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.CELCIUS_PRODUCER,
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                INSTANCE_ID,
                "23"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.LABEL_PRODUCER,
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                INSTANCE_ID,
                "23 C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleTemperatureProcessor.FLOW,
            SampleTemperatureProcessor.FAHRENHEIT_CONSUMER,
            SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
            INSTANCE_ID,
            "75"
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        mockTemperatureDatabase.assertIsSatisfied();
        mockProducerCelcius.assertIsSatisfied();
        mockProducerLabel.assertIsSatisfied();

        messageEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        flowEventReceiver.reset();
        flowEventReceiver.expectedBodiesReceived(
            toJson(new FlowStoppedEvent(SampleTemperatureProcessor.FLOW.getIdentifier().getId()))
        );
        FlowStopEvent flowStopEvent = new FlowStopEvent(SampleTemperatureProcessor.FLOW);
        producerTemplate.sendBody("direct:sendFlowEvent", flowStopEvent);
        flowEventReceiver.assertIsSatisfied();
    }

}
