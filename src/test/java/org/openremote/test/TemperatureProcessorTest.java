package org.openremote.test;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TemperatureProcessorTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorTest.class);

    @Test
    public void execute() throws Exception {

        startFlow(SampleTemperatureProcessor.FLOW.getId());

        LOG.info("##########################################################################");

        MockEndpoint mockTemperatureDatabase = context().getEndpoint("mock:temperatureDatabase", MockEndpoint.class);
        MockEndpoint mockProducerCelcius = context().getEndpoint("mock:producerCelcius", MockEndpoint.class);
        MockEndpoint mockProducerLabel = context().getEndpoint("mock:producerLabel", MockEndpoint.class);

        mockTemperatureDatabase.expectedBodiesReceived(75);
        mockProducerCelcius.expectedBodiesReceived("24");
        mockProducerLabel.expectedBodiesReceived("24 \u00B0C");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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

        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        stopFlow(SampleTemperatureProcessor.FLOW.getId());
    }

}
