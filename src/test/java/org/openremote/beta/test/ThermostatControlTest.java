package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.route.procedure.FlowStartProcedure;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ThermostatControlTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThermostatControlTest.class);

    @Produce
    ProducerTemplate producerTemplate;


    @Test
    public void readTemperature() throws Exception {

        RouteManagementService routeManagementService = context().hasService(RouteManagementService.class);
        routeManagementService.startFlowRoutes(context(), SampleTemperatureProcessor.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleThermostatControl.FLOW);

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        /* ###################################################################################### */

        mockLabelTemperature.expectedBodiesReceived("23 C");
        mockLabelSetpoint.expectedBodiesReceived("21 C");

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(75);
        producerTemplate.send("direct:" + SampleThermostatControl.TEMPERATURE_CONSUMER.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_CONSUMER.getIdentifier().getId(), exchange);

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        /* ###################################################################################### */
    }

    @Test
    public void setTemperature() throws Exception {

        RouteManagementService routeManagementService = context().hasService(RouteManagementService.class);
        routeManagementService.startFlowRoutes(context(), SampleTemperatureProcessor.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleThermostatControl.FLOW);

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);
        MockEndpoint mockProducerSetpoint = context().getEndpoint("mock:producerSetpoint", MockEndpoint.class);

        /* ###################################################################################### */

        mockLabelTemperature.expectedBodiesReceived("23 C");
        mockLabelSetpoint.expectedBodiesReceived("21 C");
        mockProducerSetpoint.expectedBodiesReceived("69", "69");

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(75);
        producerTemplate.send("direct:" + SampleThermostatControl.TEMPERATURE_CONSUMER.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_CONSUMER.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_MINUS_BUTTON.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_MINUS_BUTTON.getIdentifier().getId(), exchange);

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();
        mockProducerSetpoint.assertIsSatisfied();

        /* ###################################################################################### */

        mockLabelSetpoint.reset();
        mockLabelSetpoint.expectedBodiesReceived("25 C");
        mockProducerSetpoint.reset();
        mockProducerSetpoint.expectedBodiesReceived("79");

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(78);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_CONSUMER.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_PLUS_BUTTON.getIdentifier().getId(), exchange);

        mockLabelSetpoint.assertIsSatisfied();
        mockProducerSetpoint.assertIsSatisfied();
    }

}
