package org.openremote.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.catalog.change.ChangeNodeDescriptor;
import org.openremote.shared.event.FlowDeployEvent;
import org.openremote.shared.event.FlowRuntimeFailureEvent;
import org.openremote.shared.event.FlowStatusEvent;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.shared.event.FlowDeploymentPhase.STARTING;

public class RoutingTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingTest.class);

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

    @Test
    public void serverSideLoop() throws Exception {
        Flow flow = fromJson(
            producerTemplate.requestBody(restClientUrl("flow", "template"), null, String.class),
            Flow.class
        );

        Node nodeA = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", ChangeNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        nodeA.setLabel("TestNodeA");
        nodeA.setClientAccess(true);

        Node nodeB = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", ChangeNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        nodeB.setLabel("TestNodeB");
        nodeB.setClientAccess(true);

        Node nodeC = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", ChangeNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        nodeC.setLabel("TestNodeC");
        nodeC.setClientAccess(true);

        flow.addNode(nodeA);
        flow.addNode(nodeB);
        flow.addNode(nodeC);

        flow.addWireBetweenSlots(nodeA.getSlots()[1], nodeB.getSlots()[0]);
        flow.addWireBetweenSlots(nodeB.getSlots()[1], nodeC.getSlots()[0]);
        flow.addWireBetweenSlots(nodeC.getSlots()[1], nodeA.getSlots()[0]);

        postFlow(flow);

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flow.getId(), STARTING)),
            toJson(new FlowStatusEvent(flow.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(flow.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        eventReceiver.assertIsSatisfied();

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                nodeB.getSlots()[0],
                "abc"
            )),
            toJson(new Message(
                nodeC.getSlots()[0],
                "abc"
            )),
            toJson(new Message(
                nodeA.getSlots()[0],
                "abc"
            ))
        );
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(nodeB.getSlots()[0], "abc")); // Sink slot!
        producerTemplate.send("direct:sendEvent", exchange);
        eventReceiver.assertIsSatisfied();

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                nodeB.getSlots()[0],
                "abc"
            )),
            toJson(new Message(
                nodeC.getSlots()[0],
                "abc"
            )),
            toJson(new FlowRuntimeFailureEvent(
                flow.getId(),
                "Exchange stopped, loop detected. Message has already been processed by: TestNodeB",
                nodeB.getId()
            ))
        );
        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(nodeA.getSlots()[1], "abc")); // Source slot!
        producerTemplate.send("direct:sendEvent", exchange);
        eventReceiver.assertIsSatisfied();
    }
}
