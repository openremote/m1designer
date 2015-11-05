package org.openremote.test;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.openremote.server.catalog.change.ChangeNodeDescriptor;
import org.openremote.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.server.catalog.widget.ToggleButtonNodeDescriptor;
import org.openremote.shared.event.FlowDeployEvent;
import org.openremote.shared.event.FlowRuntimeFailureEvent;
import org.openremote.shared.event.FlowStatusEvent;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.shared.event.FlowDeploymentPhase.DEPLOYED;
import static org.openremote.shared.event.FlowDeploymentPhase.STARTING;

public class RoutingTest extends FlowIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingTest.class);
    
    @Test
    public void serverSideLoop() throws Exception {
        Flow flow = createFlow();

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

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flow.getId(), STARTING)),
            toJson(new FlowStatusEvent(flow.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(flow.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        mockEventReceiver.assertIsSatisfied();

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
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
        mockEventReceiver.assertIsSatisfied();
    }

    @Test
    public void serverSideLoop2() throws Exception {
        Flow flow = createFlow();

        Node nodeA = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", ToggleButtonNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        nodeA.setLabel("TestNodeA");

        Node nodeB = fromJson(
            producerTemplate.requestBody(restClientUrl("catalog", "node", TextLabelNodeDescriptor.TYPE), null, String.class),
            Node.class
        );
        nodeB.setLabel("TestNodeB");

        flow.addNode(nodeA);
        flow.addNode(nodeB);

        flow.addWireBetweenSlots(nodeA.getSlots()[1], nodeB.getSlots()[0]);
        flow.addWireBetweenSlots(nodeA.getSlots()[1], nodeB.getSlots()[7]);

        postFlow(flow);

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceived(
            toJson(new FlowStatusEvent(flow.getId(), STARTING)),
            toJson(new FlowStatusEvent(flow.getId(), DEPLOYED))
        );
        FlowDeployEvent flowDeployEvent = new FlowDeployEvent(flow.getId());
        producerTemplate.sendBody("direct:sendEvent", flowDeployEvent);
        mockEventReceiver.assertIsSatisfied();

        mockEventReceiver.reset();
        mockEventReceiver.expectedBodiesReceivedInAnyOrder(
            toJson(new Message(
                nodeB.getSlots()[0],
                "1"
            )),
            toJson(new Message(
                nodeB.getSlots()[7],
                "1"
            ))
        );
        Exchange exchange = new DefaultExchange(context());
        exchange.getIn().setBody(new Message(nodeA.getSlots()[1], "1"));
        producerTemplate.send("direct:sendEvent", exchange);
        mockEventReceiver.assertIsSatisfied();
    }
}
