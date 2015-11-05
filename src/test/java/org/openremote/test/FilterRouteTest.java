package org.openremote.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.catalog.filter.FilterNodeDescriptor;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class FilterRouteTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(FilterRouteTest.class);

    @EndpointInject(uri = "mock:preFilter")
    MockEndpoint mockPreFilter;


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


    @Test
    public void filterPass() throws Exception {
        Flow flow = createFlow();
        Node filterNode = createFilterNode(flow);
        Node producerNode = createProducerNode(filterNode, flow);
        postFlow(flow);
        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();
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

        postFlow(flow);
        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                ""
            ))
        );

        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], ""));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();
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

        postFlow(flow);
        startFlow(flow);

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("hello");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("false");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "false"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "false"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("0");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                filterNode.getSlots()[0],
                "0"
            ))
        );

        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(filterNode.getSlots()[0], "0"));
        producerTemplate.send("direct:sendEvent", exchange);

        mockPreFilter.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("true");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPreFilter.reset();
        mockPreFilter.expectedBodiesReceived("1");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();
    }
}
