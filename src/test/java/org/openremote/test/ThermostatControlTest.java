package org.openremote.test;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.event.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ThermostatControlTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThermostatControlTest.class);

    @Test
    public void execute() throws Exception {

        startFlow(SampleTemperatureProcessor.FLOW.getId());
        startFlow(SampleThermostatControl.FLOW.getId());

        LOG.info("##########################################################################");

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        mockLabelTemperature.expectedBodiesReceived("24 \u00B0C");
        mockLabelSetpoint.expectedBodiesReceived("21 \u00B0C");

        final String INSTANCE_ID = IdentifierUtil.generateGlobalUniqueId();

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                INSTANCE_ID,
                "24"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                INSTANCE_ID,
                "24 \u00B0C"
            )),
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_LABEL_SINK,
                INSTANCE_ID,
                "24 \u00B0C"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_CONSUMER_SINK,
                INSTANCE_ID,
                "70"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK,
                INSTANCE_ID,
                "70"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                INSTANCE_ID,
                "70"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                INSTANCE_ID,
                "21"
            )),
            toJson(new Message(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                INSTANCE_ID,
                "21 \u00B0C"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_LABEL_SINK,
                INSTANCE_ID,
                "21 \u00B0C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(
            SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
            INSTANCE_ID,
            "75"
        ));
        producerTemplate.send("direct:sendEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(
            SampleThermostatControl.SETPOINT_CONSUMER_SINK,
            INSTANCE_ID,
            "70"
        ));
        producerTemplate.send("direct:sendEvent", exchange);

        LOG.info("##########################################################################");

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        Thread.sleep(500);

        LOG.info("##########################################################################");

        MockEndpoint mockProducerSetpoint = context().getEndpoint("mock:producerSetpoint", MockEndpoint.class);
        mockProducerSetpoint.expectedBodiesReceived("69", "69");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                INSTANCE_ID,
                "69"
            )),
            toJson(new Message(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                INSTANCE_ID,
                "69"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(SampleThermostatControl.SETPOINT_MINUS_BUTTON_SOURCE, INSTANCE_ID, "1"));
        producerTemplate.send("direct:sendEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(SampleThermostatControl.SETPOINT_MINUS_BUTTON_SOURCE, INSTANCE_ID, "1"));
        producerTemplate.send("direct:sendEvent", exchange);

        LOG.info("##########################################################################");

        mockProducerSetpoint.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();
    }

}
