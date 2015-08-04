package org.openremote.beta.test;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class TemperatureProcessorTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void execute() throws Exception {

        context().hasService(RouteManagementService.class)
            .startFlowRoutes(context(), SampleTemperatureProcessor.FLOW);

        LOG.info("##########################################################################");

        MockEndpoint mockTemperatureDatabase = context().getEndpoint("mock:temperatureDatabase", MockEndpoint.class);
        MockEndpoint mockProducerCelcius = context().getEndpoint("mock:producerCelcius", MockEndpoint.class);
        MockEndpoint mockProducerLabel = context().getEndpoint("mock:producerLabel", MockEndpoint.class);

        mockTemperatureDatabase.expectedMessageCount(1);
        mockTemperatureDatabase.expectedBodiesReceived(75);
        mockProducerCelcius.expectedMessageCount(1);
        mockProducerCelcius.expectedBodiesReceived("23");
        mockProducerLabel.expectedMessageCount(1);
        mockProducerLabel.expectedBodiesReceived("23 C");

        producerTemplate.sendBody(
            "direct:" + SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getIdentifier().getId(), 75
        );

        LOG.info("##########################################################################");

        mockTemperatureDatabase.assertIsSatisfied();
        mockProducerCelcius.assertIsSatisfied();
        mockProducerLabel.assertIsSatisfied();
    }

}
