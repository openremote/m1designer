package org.openremote.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.openremote.shared.event.FlowDeployEvent;
import org.openremote.shared.event.FlowStatusEvent;
import org.openremote.shared.event.FlowStopEvent;
import org.openremote.shared.flow.Flow;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.shared.event.FlowDeploymentPhase.*;

public class FlowIntegrationTest extends IntegrationTest {

    @Produce
    protected ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:eventReceiver")
    protected MockEndpoint mockEventReceiver;

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

    protected Flow createFlow() throws Exception {
        return fromJson(
            producerTemplate.requestBody(restClientUrl("flow", "template"), null, String.class),
            Flow.class
        );
    }

    protected void postFlow(Flow flow) throws Exception {
        flow.clearDependencies();
        Exchange postFlowExchange = producerTemplate.request(
            restClientUrl("flow"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(postFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 201);
    }

    protected void putFlow(Flow flow) throws Exception {
        flow.clearDependencies();
        Exchange putFlowExchange = producerTemplate.request(
            restClientUrl("flow", flow.getId()),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.PUT);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(putFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 204);
    }

    protected void startFlow(Flow flow) throws Exception {
        startFlow(flow.getId());
    }

    protected void startFlow(String flowId) throws Exception {
        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flowId, STARTING)),
            toJson(new FlowStatusEvent(flowId, DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(flowId);
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        mockEventReceiver.assertIsSatisfied();
    }

    protected void stopFlow(Flow flow) throws Exception {
        stopFlow(flow.getId());
    }

    protected void stopFlow(String flowId) throws Exception {
        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flowId, STOPPING)),
            toJson(new FlowStatusEvent(flowId, STOPPED))
        );
        FlowStopEvent flowStopEvent = new FlowStopEvent(flowId);
        producerTemplate.sendBody("direct:sendEvent", flowStopEvent);
        mockEventReceiver.assertIsSatisfied();
    }

}
