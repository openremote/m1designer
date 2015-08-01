package org.openremote.beta.test;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.route.FlowRoute;
import org.openremote.beta.server.route.NodeRoute;
import org.openremote.beta.server.route.SubflowRoute;
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
import static org.openremote.beta.server.route.FlowRoute.DESTINATION_SINK_ID;

public class EnvironmentWidgetTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentWidgetTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    FlowRoute environmentPanelRoute;
    FlowRoute temperatureProcessorRoute;
    FlowRoute thermostatControlRoute;

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        environmentPanelRoute = new FlowRoute(context(), SampleEnvironmentWidget.FLOW);
        temperatureProcessorRoute = new FlowRoute(context(), SampleTemperatureProcessor.FLOW);
        thermostatControlRoute = new FlowRoute(context(), SampleThermostatControl.FLOW);
        return new RouteBuilder[] {
            environmentPanelRoute,
            temperatureProcessorRoute,
            thermostatControlRoute
        };
    }

    @Test
    public void readTemperature() throws Exception {

        environmentPanelRoute.startRoutes();
        temperatureProcessorRoute.startRoutes();
        thermostatControlRoute.startRoutes();

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        Map<String, Object> headers = new HashMap<>();

        /* ###################################################################################### */

        mockLivingroomSetpointActuator.expectedMessageCount(0);
        mockBedroomSetpointActuator.expectedMessageCount(0);

        mockLabelTemperature.expectedMessageCount(2);
        mockLabelTemperature.expectedMessagesMatches(
            and(
                header(NodeRoute.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("23 C")
            ),
            and(
                header(NodeRoute.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("18 C")
            )
        );

        mockLabelSetpoint.expectedMessageCount(2);
        mockLabelSetpoint.expectedMessagesMatches(
            and(
                header(NodeRoute.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("21 C")
            ),
            and(
                header(NodeRoute.NODE_INSTANCE_ID).isEqualTo(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()),
                bodyAs(String.class).isEqualTo("16 C")
            )
        );

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK.getIdentifier().getId());
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(75);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK.getIdentifier().getId());
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.BEDROOM_TEMPERATURE_SENSOR_SINK.getIdentifier().getId());
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(65);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.BEDROOM_SETPOINT_SENSOR_SINK.getIdentifier().getId());
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(62);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        /* ###################################################################################### */
    }

    @Test
    public void setTemperature() throws Exception {

        environmentPanelRoute.startRoutes();
        temperatureProcessorRoute.startRoutes();
        thermostatControlRoute.startRoutes();

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        Map<String, Object> headers = new HashMap<>();

        /* ###################################################################################### */

        mockLabelSetpoint.expectedMessageCount(2);
        mockLabelSetpoint.whenAnyExchangeReceived(result -> {
                if (result.getIn().getHeader(NodeRoute.NODE_INSTANCE_ID, String.class)
                    .equals(SampleEnvironmentWidget.BEDROOM_THERMOSTAT.getIdentifier().getId()))
                    assertEquals(result.getIn().getBody(String.class), "18 C");

                if (result.getIn().getHeader(NodeRoute.NODE_INSTANCE_ID, String.class)
                    .equals(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId()))
                    assertEquals(result.getIn().getBody(String.class), "21 C");
            }
        );

        mockLivingroomSetpointActuator.expectedMessageCount(2);
        mockLivingroomSetpointActuator.expectedBodiesReceived("69", "69");

        mockBedroomSetpointActuator.expectedMessageCount(0);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK.getIdentifier().getId());
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(70);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleEnvironmentWidget.BEDROOM_SETPOINT_SENSOR_SINK.getIdentifier().getId());
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(65);
        producerTemplate.send("direct:" + SampleEnvironmentWidget.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK.getIdentifier().getId());
        Stack<String> correlationStack = new Stack<>();
        correlationStack.push(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId());
        headers.put(SubflowRoute.SUBFLOW_CORRELATION_STACK, correlationStack);
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.FLOW.getIdentifier().getId(), exchange);

        headers.clear();
        headers.put(DESTINATION_SINK_ID, SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK.getIdentifier().getId());
        correlationStack = new Stack<>();
        correlationStack.push(SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId());
        headers.put(SubflowRoute.SUBFLOW_CORRELATION_STACK, correlationStack);
        exchange = new DefaultExchange(context());
        exchange.getIn().setHeaders(headers);
        exchange.getIn().setBody(null);
        producerTemplate.send("direct:" + SampleThermostatControl.FLOW.getIdentifier().getId(), exchange);

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();

        /* ###################################################################################### */

    }

}
