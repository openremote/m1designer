package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.event.FlowStartEvent;
import org.openremote.beta.shared.event.FlowStartedEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class EnvironmentWidgetTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentWidgetTest.class);

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
        flowEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new FlowStartedEvent(SampleTemperatureProcessor.FLOW.getIdentifier().getId())),
            toJson(new FlowStartedEvent(SampleThermostatControl.FLOW.getIdentifier().getId())),
            toJson(new FlowStartedEvent(SampleEnvironmentWidget.FLOW.getIdentifier().getId()))
        );

        producerTemplate.sendBody("direct:sendFlowEvent", new FlowStartEvent(SampleTemperatureProcessor.FLOW));
        producerTemplate.sendBody("direct:sendFlowEvent", new FlowStartEvent(SampleThermostatControl.FLOW));
        producerTemplate.sendBody("direct:sendFlowEvent", new FlowStartEvent(SampleEnvironmentWidget.FLOW));

        flowEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);

        mockLivingroomSetpointActuator.expectedMessageCount(0);
        mockBedroomSetpointActuator.expectedMessageCount(0);

        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleEnvironmentWidget.FLOW,
                SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR,
                SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK,
                "75"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.TEMPERATURE_CONSUMER,
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.CELCIUS_PRODUCER,
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "23"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.LABEL_PRODUCER,
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.TEMPERATURE_LABEL,
                SampleThermostatControl.TEMPERATURE_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.FLOW,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK,
                "70"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_CONSUMER,
                SampleThermostatControl.SETPOINT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER,
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.CELCIUS_PRODUCER,
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "21"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FLOW,
                SampleTemperatureProcessor.LABEL_PRODUCER,
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "21 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_LABEL,
                SampleThermostatControl.SETPOINT_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "21 C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleEnvironmentWidget.FLOW,
            SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR,
            SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK,
            "75"
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleEnvironmentWidget.FLOW,
            SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR,
            SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK,
            "70"
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        messageEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.reset();
        mockLivingroomSetpointActuator.expectedMessageCount(1);
        mockBedroomSetpointActuator.reset();
        mockBedroomSetpointActuator.expectedMessageCount(0);

        messageEventReceiver.reset();
        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_MINUS_BUTTON,
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_PRODUCER,
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "69"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.FLOW,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_MINUS_BUTTON,
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.FLOW,
                SampleThermostatControl.SETPOINT_PRODUCER,
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
                "69"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.FLOW,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR,
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleThermostatControl.FLOW,
            SampleThermostatControl.SETPOINT_MINUS_BUTTON,
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
            null
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleThermostatControl.FLOW,
            SampleThermostatControl.SETPOINT_MINUS_BUTTON,
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getIdentifier().getId(),
            null
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        messageEventReceiver.assertIsSatisfied();
    }

}
