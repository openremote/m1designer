package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.openremote.beta.shared.event.FlowStopEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

public class EventServiceTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventServiceTest.class);

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
                    .to(createWebSocketUri("events"));

                from(createWebSocketUri("events"))
                    .to("log:EVENT_RECEIVED: ${body}")
                    .to("mock:eventReceiver");

            }
        };
    }

    @Test
    public void echo() throws Exception {

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody(createWebSocketUri("events"), flowDeployEvent);
        mockEventReceiver.assertIsSatisfied();

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STOPPING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STOPPED))
        );
        FlowStopEvent flowStopEvent = new FlowStopEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody(createWebSocketUri("events"), flowStopEvent);
        mockEventReceiver.assertIsSatisfied();
    }

}
