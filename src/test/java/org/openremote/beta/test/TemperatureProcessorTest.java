package org.openremote.beta.test;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.beta.server.route.FlowRoute;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TemperatureProcessorTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureProcessorTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    FlowRoute flowRoute;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        flowRoute = new FlowRoute(context(), SampleTemperatureProcessor.FLOW);
        return flowRoute;
    }

    @Test
    public void execute() throws Exception {

        LOG.info("##########################################################################");

        flowRoute.startRoutes();

        LOG.info("##########################################################################");

        flowRoute.removeRoutesFromCamelContext();

        LOG.info("##########################################################################");

        flowRoute.addRoutesToCamelContext();
        flowRoute.startRoutes();

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

        Map<String, Object> headers = new HashMap<>();
        headers.put(FlowRoute.DESTINATION_SINK_ID, SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK.getIdentifier().getId());
        producerTemplate.sendBodyAndHeaders(
            "direct:" + SampleTemperatureProcessor.FLOW.getIdentifier().getId(), 75, headers
        );

        LOG.info("##########################################################################");

        mockTemperatureDatabase.assertIsSatisfied();
        mockProducerCelcius.assertIsSatisfied();
        mockProducerLabel.assertIsSatisfied();
    }

}
