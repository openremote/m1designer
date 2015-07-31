package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.MessageHistory;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.route.FlowRouteManager;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openremote.beta.server.route.FlowRouteManager.DESTINATION_SINK_ID;

public class ThermostatControlTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThermostatControlTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    FlowRouteManager temperatureProcessorManager;
    FlowRouteManager thermostatControlManager;

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        temperatureProcessorManager = new FlowRouteManager(context(), SampleTemperatureProcessor.FLOW);
        thermostatControlManager = new FlowRouteManager(context(), SampleThermostatControl.FLOW);
        return new RouteBuilder[] {
            temperatureProcessorManager,
            thermostatControlManager
        };
    }

    @Test
    public void execute() throws Exception {

        temperatureProcessorManager.startRoutes();
        thermostatControlManager.startRoutes();

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);
        MockEndpoint mockProducerSetpoint = context().getEndpoint("mock:producerSetpoint", MockEndpoint.class);

        Map<String, Object> headers = new HashMap<>();

        /* ###################################################################################### */

        mockLabelTemperature.expectedBodiesReceived("23 C");
        mockLabelSetpoint.expectedBodiesReceived("21 C");

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleThermostatControl.TEMPERATURE_CONSUMER_SINK.getIdentifier().getId());
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(75);
        producerTemplate.send("direct:" + SampleThermostatControl.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleThermostatControl.SETPOINT_CONSUMER_SINK.getIdentifier().getId());
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleThermostatControl.FLOW.getIdentifier().getId(), exchange);

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        /* ###################################################################################### */

    }

}
