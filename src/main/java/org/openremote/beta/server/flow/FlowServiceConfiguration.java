package org.openremote.beta.server.flow;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.openremote.beta.server.DefaultConfiguration;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.shared.util.Util.*;

public class FlowServiceConfiguration extends DefaultConfiguration {

    private static final Logger LOG = Logger.getLogger(FlowServiceConfiguration.class.getName());

    // TODO: This is test/sample data
    static Flow[] flows = new Flow[20];

    static {
        Slot httpRequestSlot = new Slot(UUID.randomUUID().toString(), Slot.Type.SOURCE);
        Node httpRequest = new Node(
            UUID.randomUUID().toString(),
            "HTTP Listener",
            "GET /foo",
            httpRequestSlot
        );

        Map<String, Object> properties = createMap();
        properties.put("address", "0.0.0.0");
        properties.put("port", 1234);
        properties.put("method", "GET");
        properties.put("path", "/foo");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("x", 50);
        editor.put("y", 50);
        httpRequest.setProperties(properties);

        Slot groovyTemplateInSlot = new Slot(UUID.randomUUID().toString(), Slot.Type.SINK);
        Slot groovyTemplateOutSlot = new Slot(UUID.randomUUID().toString(), Slot.Type.SOURCE, "out");
        Slot groovyTemplateOutSlot2 = new Slot(UUID.randomUUID().toString(), Slot.Type.SOURCE, "out2");
        Slot groovyTemplateOutSlot3 = new Slot(UUID.randomUUID().toString(), Slot.Type.SOURCE, "out3");
        Node groovyTemplate = new Node(
            UUID.randomUUID().toString(),
            "Groovy Processor",
            "Some template",
            groovyTemplateInSlot, groovyTemplateOutSlot, groovyTemplateOutSlot2, groovyTemplateOutSlot3
        );
        properties = createMap();
        properties.put("script", "return request.getHeader('foo');");
        editor = createMap(properties, "editor");
        editor.put("x", 350);
        editor.put("y", 100);
        groovyTemplate.setProperties(properties);

        Slot httpResponseSlot = new Slot(UUID.randomUUID().toString(), Slot.Type.SINK);
        Node httpResponse = new Node(
            UUID.randomUUID().toString(),
            "HTTP Responder",
            "Return message",
            httpResponseSlot
        );
        properties = createMap();
        editor = createMap(properties, "editor");
        editor.put("x", 700);
        editor.put("y", 200);
        httpResponse.setProperties(properties);

        Node[] nodes = new Node[3]; // TODO Make this larger than three to generate more performance testing items
        nodes[0] = httpRequest;
        nodes[1] = groovyTemplate;
        nodes[2] = httpResponse;
        for (int i = 3; i < nodes.length; i++) {
            nodes[i] = new Node(
                UUID.randomUUID().toString(),
                "Foo " + i,
                "Bar " + i,
                groovyTemplateInSlot, groovyTemplateOutSlot, groovyTemplateOutSlot2, groovyTemplateOutSlot3
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("x", 700 + (i * 10));
            editor.put("y", 200 + (i * 10));
            nodes[i].setProperties(properties);
        }

        flows[0] = new Flow(
            UUID.randomUUID().toString(),
            "Some HTTP request/response flow",
            nodes,
            new Wire[]{
                new Wire(httpRequestSlot.getId(), groovyTemplateInSlot.getId()),
                new Wire(groovyTemplateOutSlot.getId(), httpResponseSlot.getId())
            }
        );

        nodes = new Node[2];
        nodes[0] = new Node(UUID.randomUUID().toString(), "ZWaveSensor", "Office Temperature", new Slot(UUID.randomUUID().toString(), Slot.Type.SOURCE));
        properties = createMap();
        editor = createMap(properties, "editor");
        editor.put("x", 100);
        editor.put("y", 100);
        nodes[0].setProperties(properties);
        nodes[1] = new Node(UUID.randomUUID().toString(), "MySQL", "Temperature Timeseries Storage", new Slot(UUID.randomUUID().toString(), Slot.Type.SINK));
        properties = createMap();
        editor = createMap(properties, "editor");
        editor.put("x", 500);
        editor.put("y", 200);
        nodes[1].setProperties(properties);
        flows[1] = new Flow(
            UUID.randomUUID().toString(),
            "My Office Temperature Flow",
            nodes,
            new Wire[]{
                new Wire(nodes[0].getSlots()[0].getId(), nodes[1].getSlots()[0].getId()),
            }
        );

        flows[2] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data with a veryveryveryveryveryveryveryveryveryveryveryveryvery long label 1"
        );
        flows[3] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 2"
        );
        flows[4] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 3"
        );
        flows[5] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 4"
        );
        flows[6] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 5"
        );
        flows[7] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 6"
        );
        flows[8] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 7"
        );
        flows[9] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 8"
        );
        flows[10] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 9"
        );
        flows[11] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 10"
        );
        flows[12] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 11"
        );
        flows[13] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 12"
        );
        flows[14] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 13"
        );
        flows[15] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 14"
        );
        flows[16] = new Flow(
            UUID.randomUUID().toString(),
            "Random test data 15"
        );
        flows[17] = new Flow(
            UUID.randomUUID().toString(),
            "16"
        );
        flows[18] = new Flow(
            UUID.randomUUID().toString(),
            "17"
        );
        flows[19] = new Flow(
            UUID.randomUUID().toString(),
            "18"
        );

    }

    class FlowServiceRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/flow")

                .get()
                .route().id("GET all flows")
                .process(exchange -> {

                    Flow[] flowsInfo = new Flow[flows.length];
                    for (int i = 0; i < flowsInfo.length; i++) {
                        Flow flow = flows[i];
                        flowsInfo[i] = new Flow(flow.getId(), flow.getLabel());
                    }

                    exchange.getOut().setBody(flowsInfo);
                })
                .endRest()

                .get("{id}")
                .route().id("GET flow by ID")
                .process(exchange -> {

                    for (Flow flow : flows) {
                        if (flow.getId().equals(exchange.getIn().getHeader(("id")))) {
                            exchange.getOut().setBody(flow);
                        }
                    }
                    if (exchange.getOut().getBody() == null)
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);

                })
                .endRest();
        }
    }

    @Override
    public void apply(CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new FlowServiceRouteBuilder());
    }

}
