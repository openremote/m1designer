package org.openremote.test;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.testdata.SampleEnvironmentWidget;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class EnvironmentWidgetTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentWidgetTest.class);

    @Test
    public void execute() throws Exception {
        
        startFlow(SampleTemperatureProcessor.FLOW.getId());
        startFlow(SampleThermostatControl.FLOW.getId());
        startFlow(SampleEnvironmentWidget.FLOW.getId());
        
        LOG.info("##########################################################################");

        MockEndpoint mockLivingroomSetpointActuator = context().getEndpoint("mock:livingroomSetpointActuator", MockEndpoint.class);
        MockEndpoint mockBedroomSetpointActuator = context().getEndpoint("mock:bedroomSetpointActuator", MockEndpoint.class);

        mockLivingroomSetpointActuator.expectedMessageCount(0);
        mockBedroomSetpointActuator.expectedMessageCount(0);

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK,
                "75"
            )),
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT_TEMPERATURE_SINK,
                "75"
            )),
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "75"
            )),
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "75"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "75"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "24"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "24 \u00B0C"
            )),
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "24 \u00B0C"
            )),
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK,
                "70"
            )),
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT_SETPOINT_SINK,
                "70"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "70"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "70"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "70"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21 \u00B0C"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_LABEL_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "21 \u00B0C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(SampleEnvironmentWidget.LIVINGROOM_TEMPERATURE_SENSOR_SINK, "75"));
        producerTemplate.send("direct:sendEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(SampleEnvironmentWidget.LIVINGROOM_SETPOINT_SENSOR_SINK, "70"));
        producerTemplate.send("direct:sendEvent", exchange);

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.reset();
        mockLivingroomSetpointActuator.expectedMessageCount(1);
        mockBedroomSetpointActuator.reset();
        mockBedroomSetpointActuator.expectedMessageCount(0);

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "69"
            )),
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
                "69"
            )),
            toJson(new Message(
                SampleEnvironmentWidget.LIVINGROOM_SETPOINT_ACTUATOR_SINK,
                "69"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SOURCE,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
            "1"
        ));
        producerTemplate.send("direct:sendEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(
            SampleThermostatControl.SETPOINT_MINUS_BUTTON_SOURCE,
            SampleEnvironmentWidget.LIVINGROOM_THERMOSTAT.getId(),
            "1"
        ));
        producerTemplate.send("direct:sendEvent", exchange);

        LOG.info("##########################################################################");

        mockLivingroomSetpointActuator.assertIsSatisfied();
        mockBedroomSetpointActuator.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();
    }

}
