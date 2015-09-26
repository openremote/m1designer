package org.openremote.beta.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.beta.server.catalog.filter.FilterNodeDescriptor;
import org.openremote.beta.shared.event.FlowDeployEvent;
import org.openremote.beta.shared.event.FlowStatusEvent;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.shared.event.FlowDeploymentPhase.*;

public class FilterRouteTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRouteTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:eventReceiver")
    MockEndpoint eventReceiver;

    @EndpointInject(uri = "mock:preFilter")
    MockEndpoint mockPreFilter;

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

    protected Node createFilterNode(Flow flow) throws Exception {
        Node filterNode = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", FilterNodeDescriptor.TYPE), null, String.class),
            Node.class
        );

        filterNode.setClientAccess(true);
        filterNode.setPreEndpoint("mock:preFilter");
        flow.addNode(filterNode);

        return filterNode;
    }

    protected Node createProducerNode(Node filterNode, Flow flow) throws Exception {
        Node producerNode = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", Node.TYPE_PRODUCER), null, String.class),
            Node.class
        );

        producerNode.setClientAccess(true);
        flow.addNode(producerNode);

        flow.addWireBetweenSlots(filterNode.getSlots()[2], producerNode.getSlots()[0]);

        return producerNode;
    }

    protected void startFlow(Flow flow) throws Exception {
        Exchange postFlowExchange = producerTemplate.request(
            restClientUrl("flow"),
            exchange -> {
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                exchange.getIn().setBody(toJson(flow));
            }
        );
        assertEquals(postFlowExchange.getOut().getHeader(HTTP_RESPONSE_CODE), 201);

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flow.getId(), STARTING)),
            toJson(new FlowStatusEvent(flow.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(flow.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        eventReceiver.assertIsSatisfied();
    }

    @Test
    public void filterPass() throws Exception {
        Flow flow = createFlow();
        Node filterNode = createFilterNode(flow);
        Node producerNode = createProducerNode(filterNode, flow);
        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "hello"
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                "hello"
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "hello"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();
    }

    @Test
    public void filterDropEmpty() throws Exception {
        Flow flow = createFlow();
        Node filterNode = createFilterNode(flow);

        Node producerNode = createProducerNode(filterNode, flow);

        filterNode.setProperties(toJson(
            fromJson(filterNode.getProperties(), ObjectNode.class)
                .put("dropEmpty", true)
        ));

        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                ""
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], ""));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "hello"
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                "hello"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "hello"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();
    }

    @Test
    public void filterDropFalse() throws Exception {
        Flow flow = createFlow();
        Node filterNode = createFilterNode(flow);

        Node producerNode = createProducerNode(filterNode, flow);

        filterNode.setProperties(toJson(
            fromJson(filterNode.getProperties(), ObjectNode.class)
                .put("dropFalse", true)
        ));

        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                ""
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                ""
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], ""));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "hello"
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                "hello"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "hello"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("false");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "false"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "false"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("0");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "0"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "0"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("true");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "true"
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                "true"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "true"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("1");

        eventReceiver.reset();
        eventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "1"
            )),
            toJson(new Message(
                producerNode.getSlots()[0],
                "1"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "1"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        eventReceiver.assertIsSatisfied();
    }
}
