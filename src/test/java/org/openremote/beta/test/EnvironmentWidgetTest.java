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
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.STARTING;

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
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), DEPLOYED)),
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), DEPLOYED)),
            toJson(new FlowStatusEvent(SampleEnvironmentWidget.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleEnvironmentWidget.FLOW.getId(), DEPLOYED))
        );

        producerTemplate.sendBody("direct:sendFlowEvent", new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId()));
        producerTemplate.sendBody("direct:sendFlowEvent", new FlowDeployEvent(SampleThermostatControl.FLOW.getId()));
        producerTemplate.sendBody("direct:sendFlowEvent", new FlowDeployEvent(SampleEnvironmentWidget.FLOW.getId()));

        flowEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);

        mockLivingroomSetpointActuator.expectedMessageCount(0);
        mockBedroomSetpointActuator.expectedMessageCount(0);

        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK,
                "75"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "23"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.TEMPERATURE_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK,
                "70"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21 C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK, "75"));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK, "70"));
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
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "69"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "69"
            )),
            toJson(new MessageEvent(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
            null
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
            null
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        messageEventReceiver.assertIsSatisfied();
    }

}
