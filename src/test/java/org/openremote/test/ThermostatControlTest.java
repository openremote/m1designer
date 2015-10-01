package org.openremote.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.event.FlowDeployEvent;
import org.openremote.shared.event.Message;
import org.openremote.shared.event.FlowStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.shared.event.FlowDeploymentPhase.STARTING;

public class ThermostatControlTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThermostatControlTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:eventReceiver")
    MockEndpoint eventReceiver;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:sendEvent")
                    .to(websocketClientUrl("events"));

                from(websocketClientUrl("events"))
                    .to("log:EVENT_RECEIVED: ${body}")
                    .to("mock:eventReceiver");
            }
        };
    }

    @Test
    public void execute() throws Exception {

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        eventReceiver.assertIsSatisfied();

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), DEPLOYED))
        );
        flowDeployEvent = new FlowDeployEvent(SampleThermostatControl.FLOW.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        mockLabelTemperature.expectedBodiesReceived("24 \u00B0C");
        mockLabelSetpoint.expectedBodiesReceived("21 \u00B0C");

        final String INSTANCE_ID = IdentifierUtil.generateGlobalUniqueId();

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
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
        eventReceiver.assertIsSatisfied();

        Thread.sleep(500);

        LOG.info("##########################################################################");

        MockEndpoint mockProducerSetpoint = context().getEndpoint("mock:producerSetpoint", MockEndpoint.class);
        mockProducerSetpoint.expectedBodiesReceived("69", "69");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
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
        eventReceiver.assertIsSatisfied();
    }

}
