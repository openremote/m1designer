package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.shared.event.FlowStartEvent;
import org.openremote.beta.shared.event.FlowStartedEvent;
import org.openremote.beta.shared.event.FlowStopEvent;
import org.openremote.beta.shared.event.FlowStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class EventTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:eventReceiver")
    MockEndpoint mockEventReceiver;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:sendFlowEvent")
                    .to(createWebSocketUri("flow"));

                from(createWebSocketUri("flow"))
                    .to("log:FLOW_EVENT_RECEIVED: ${body}")
                    .to("mock:eventReceiver");

            }
        };
    }

    @Test
    public void echo() throws Exception {

        mockEventReceiver.reset();
        mockEventReceiver.expectedMessageCount(1);
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStartedEvent(SampleTemperatureProcessor.FLOW.getIdentifier().getId()))
        );
        FlowStartEvent flowStartEvent = new FlowStartEvent(SampleTemperatureProcessor.FLOW);
        producerTemplate.sendBody(createWebSocketUri("flow"), flowStartEvent);
        mockEventReceiver.assertIsSatisfied();

        mockEventReceiver.reset();
        mockEventReceiver.expectedMessageCount(1);
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStoppedEvent(SampleTemperatureProcessor.FLOW.getIdentifier().getId()))
        );
        FlowStopEvent flowStopEvent = new FlowStopEvent(SampleTemperatureProcessor.FLOW);
        producerTemplate.sendBody(createWebSocketUri("flow"), flowStopEvent);
        mockEventReceiver.assertIsSatisfied();
    }

}
