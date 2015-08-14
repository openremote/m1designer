package org.openremote.beta.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.server.util.IdentifierUtil;
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.STARTING;

public class ThermostatControlTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ThermostatControlTest.class);

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
        flowEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleTemperatureProcessor.FLOW.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId());
        producerTemplate.sendBody("direct:sendFlowEvent", flowDeployEvent);
        flowEventReceiver.assertIsSatisfied();

        flowEventReceiver.reset();
        flowEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), STARTING)),
            toJson(new FlowStatusEvent(SampleThermostatControl.FLOW.getId(), DEPLOYED))
        );
        flowDeployEvent = new FlowDeployEvent(SampleThermostatControl.FLOW.getId());
        producerTemplate.sendBody("direct:sendFlowEvent", flowDeployEvent);
        flowEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockLabelTemperature = context().getEndpoint("mock:labelTemperature", MockEndpoint.class);
        MockEndpoint mockLabelSetpoint = context().getEndpoint("mock:labelSetpoint", MockEndpoint.class);

        mockLabelTemperature.expectedBodiesReceived("23 C");
        mockLabelSetpoint.expectedBodiesReceived("21 C");

        final String INSTANCE_ID = IdentifierUtil.generateGlobalUniqueId();

        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                INSTANCE_ID,
                "75"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                INSTANCE_ID,
                "23"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                INSTANCE_ID,
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.TEMPERATURE_LABEL_SINK,
                INSTANCE_ID,
                "23 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_CONSUMER_SINK,
                INSTANCE_ID,
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK,
                INSTANCE_ID,
                "70"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.CELCIUS_PRODUCER_SINK,
                INSTANCE_ID,
                "21"
            )),
            toJson(new MessageEvent(
                SampleTemperatureProcessor.LABEL_PRODUCER_SINK,
                INSTANCE_ID,
                "21 C"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_LABEL_SINK,
                INSTANCE_ID,
                "21 C"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(
            SampleThermostatControl.TEMPERATURE_CONSUMER_SINK,
            INSTANCE_ID,
            "75"
        ));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(SampleThermostatControl.SETPOINT_CONSUMER_SINK, INSTANCE_ID, "70"));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        LOG.info("##########################################################################");

        mockLabelTemperature.assertIsSatisfied();
        mockLabelSetpoint.assertIsSatisfied();
        messageEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        MockEndpoint mockProducerSetpoint = context().getEndpoint("mock:producerSetpoint", MockEndpoint.class);
        mockProducerSetpoint.expectedBodiesReceived("69", "69");

        messageEventReceiver.reset();
        messageEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                INSTANCE_ID,
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                INSTANCE_ID,
                "69"
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK,
                INSTANCE_ID,
                null
            )),
            toJson(new MessageEvent(
                SampleThermostatControl.SETPOINT_PRODUCER_SINK,
                INSTANCE_ID,
                "69"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK, INSTANCE_ID, null));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new MessageEvent(SampleThermostatControl.SETPOINT_MINUS_BUTTON_SINK, INSTANCE_ID, null));
        producerTemplate.send("direct:sendMessageEvent", exchange);

        LOG.info("##########################################################################");

        mockProducerSetpoint.assertIsSatisfied();
        messageEventReceiver.assertIsSatisfied();
    }

}
