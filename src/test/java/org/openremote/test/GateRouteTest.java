package org.openremote.test;

import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.catalog.gate.AndNodeDescriptor;
import org.openremote.server.catalog.gate.NotNodeDescriptor;
import org.openremote.server.catalog.gate.OrNodeDescriptor;
import org.openremote.server.catalog.gate.XorNodeDescriptor;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class GateRouteTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(GateRouteTest.class);

    protected Node createGateNode(Flow flow, String nodeType) throws Exception {
        Node node = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", nodeType), null, String.class),
            Node.class
        );

        node.setClientAccess(true);
        node.setPostEndpoint("mock:postGate");
        flow.addNode(node);

        return node;
    }

    @Test
    public void NOT() throws Exception {
        Flow flow = createFlow();
        Node gateNode = createGateNode(flow, NotNodeDescriptor.TYPE);

        postFlow(flow);
        startFlow(flow);

        MockEndpoint mockPostGate = (MockEndpoint) context().getEndpoint("mock:postGate");

        LOG.info("##########################################################################");

        mockPostGate.reset();
        mockPostGate.expectedBodiesReceived("0");
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(gateNode.getSlots()[0], "1"));
        producerTemplate.send("direct:sendEvent", exchange);
        mockPostGate.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPostGate.reset();
        mockPostGate.expectedBodiesReceived("1");
        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(gateNode.getSlots()[0], "0"));
        producerTemplate.send("direct:sendEvent", exchange);
        mockPostGate.assertIsSatisfied();

        LOG.info("##########################################################################");

        mockPostGate.reset();
        mockPostGate.expectedMessageCount(0);
        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(gateNode.getSlots()[0], "NOT A BOOLEAN"));
        producerTemplate.send("direct:sendEvent", exchange);
        mockPostGate.assertIsSatisfied();
    }

    @Test
    public void AND() throws Exception {
        Flow flow = createFlow();
        Node gateNode = createGateNode(flow, AndNodeDescriptor.TYPE);
        postFlow(flow);

        testGate(flow, gateNode, new String[] {"0", "0"}, new String[] {"0", "0"});
        testGate(flow, gateNode, new String[] {"0", "1"}, new String[] {"0", "0"});
        testGate(flow, gateNode, new String[] {"1", "0"}, new String[] {"0", "0"});
        testGate(flow, gateNode, new String[] {"1", "1"}, new String[] {"0", "1"});
        testGate(flow, gateNode, new String[] {"NOT A BOOLEAN", "ALSO NOT A BOOLEAN"}, new String[0]);
    }

    @Test
    public void OR() throws Exception {
        Flow flow = createFlow();
        Node gateNode = createGateNode(flow, OrNodeDescriptor.TYPE);
        postFlow(flow);

        testGate(flow, gateNode, new String[] {"0", "0"}, new String[] {"0", "0"});
        testGate(flow, gateNode, new String[] {"0", "1"}, new String[] {"0", "1"});
        testGate(flow, gateNode, new String[] {"1", "0"}, new String[] {"1", "1"});
        testGate(flow, gateNode, new String[] {"1", "1"}, new String[] {"1", "1"});
        testGate(flow, gateNode, new String[] {"NOT A BOOLEAN", "ALSO NOT A BOOLEAN"}, new String[0]);
    }

    @Test
    public void XOR() throws Exception {
        Flow flow = createFlow();
        Node gateNode = createGateNode(flow, XorNodeDescriptor.TYPE);
        postFlow(flow);

        testGate(flow, gateNode, new String[] {"0", "0"}, new String[] {"0", "0"});
        testGate(flow, gateNode, new String[] {"0", "1"}, new String[] {"0", "1"});
        testGate(flow, gateNode, new String[] {"1", "0"}, new String[] {"1", "1"});
        testGate(flow, gateNode, new String[] {"1", "1"}, new String[] {"1", "0"});
        testGate(flow, gateNode, new String[] {"NOT A BOOLEAN", "ALSO NOT A BOOLEAN"}, new String[0]);
    }

    public void testGate(Flow flow, Node gateNode, String[] input, String[] expectedOutput) throws Exception {
        startFlow(flow);

        LOG.info("##########################################################################");

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new Message(gateNode.getSlots()[0], input[0])),
            toJson(new Message(gateNode.getSlots()[1], input[1]))
        );
        MockEndpoint mockPostGate = (MockEndpoint) context().getEndpoint("mock:postGate");
        if (expectedOutput.length > 0) {
            mockPostGate.expectedBodiesReceived(expectedOutput[0], expectedOutput[1]);
        } else {
            mockPostGate.expectedMessageCount(0);
        }
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(gateNode.getSlots()[0], input[0]));
        producerTemplate.send("direct:sendEvent", exchange);
        exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(gateNode.getSlots()[1], input[1]));
        producerTemplate.send("direct:sendEvent", exchange);
        mockPostGate.assertIsSatisfied();
        mockEventReceiver.assertIsSatisfied();

        LOG.info("##########################################################################");

        stopFlow(flow);
    }

}
