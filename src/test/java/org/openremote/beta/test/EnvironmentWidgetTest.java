package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.route.FlowRoutes;
import org.openremote.beta.server.route.RouteConstants;
import org.openremote.beta.server.route.RouteManagementService;
import org.openremote.beta.server.route.procedure.FlowStartProcedure;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static org.apache.camel.builder.PredicateBuilder.and;
import static org.openremote.beta.server.route.RouteConstants.SUBFLOW_CORRELATION_STACK;

public class EnvironmentWidgetTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentWidgetTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void readTemperature() throws Exception {

        RouteManagementService routeManagementService = context().hasService(RouteManagementService.class);
        routeManagementService.startFlowRoutes(context(), SampleEnvironmentWidget.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleTemperatureProcessor.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleThermostatControl.FLOW);

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);


        /* ###################################################################################### */

        mockLivingroomSetpointActuator.expectedMessageCount(0);
        mockBedroomSetpointActuator.expectedMessageCount(0);

        mockLabelTemperature.expectedMessageCount(2);
        mockLabelTemperature.expectedMessagesMatches(
            and(
                header(RouteConstants.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("23 C")
            ),
            and(
                header(RouteConstants.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("18 C")
            )
        );

        mockLabelSetpoint.expectedMessageCount(2);
        mockLabelSetpoint.expectedMessagesMatches(
            and(
                header(RouteConstants.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("21 C")
            ),
            and(
                header(RouteConstants.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("16 C")
            )
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(75);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(65);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.BEDROOM_TEMPERATURE_SENSOR.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(62);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.BEDROOM_SETPOINT_SENSOR.getIdentifier().getId(), exchange);

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        /* ###################################################################################### */
    }

    @Test
    public void setTemperature() throws Exception {

        RouteManagementService routeManagementService = context().hasService(RouteManagementService.class);
        routeManagementService.startFlowRoutes(context(), SampleEnvironmentWidget.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleTemperatureProcessor.FLOW);
        routeManagementService.startFlowRoutes(context(), SampleThermostatControl.FLOW);

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        Map<String, Object> headers = new HashMap<>();

        /* ###################################################################################### */

        mockLabelSetpoint.expectedMessageCount(2);
        mockLabelSetpoint.whenAnyExchangeReceived(result -> {
                if (result.getIn().getHeader(RouteConstants.NODE_INSTANCE_ID, String.class)
                    .equals(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()))
                    assertEquals(result.getIn().getBody(String.class), "18 C");

                if (result.getIn().getHeader(RouteConstants.NODE_INSTANCE_ID, String.class)
                    .equals(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()))
                    assertEquals(result.getIn().getBody(String.class), "21 C");
            }
        );

        mockLivingroomSetpointActuator.expectedMessageCount(2);
        mockLivingroomSetpointActuator.expectedBodiesReceived("69", "69");

        mockBedroomSetpointActuator.expectedMessageCount(0);

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR.getIdentifier().getId(), exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(65);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.BEDROOM_SETPOINT_SENSOR.getIdentifier().getId(), exchange);

        headers.clear();
        Stack<String> correlationStack = new Stack<>();
        correlationStack.push(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId());
        headers.put(SUBFLOW_CORRELATION_STACK, correlationStack);
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_MINUS_BUTTON.getIdentifier().getId(), exchange);

        headers.clear();
        correlationStack = new Stack<>();
        correlationStack.push(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId());
        headers.put(SUBFLOW_CORRELATION_STACK, correlationStack);
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.SETPOINT_MINUS_BUTTON.getIdentifier().getId(), exchange);

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();

        /* ###################################################################################### */

    }

}
