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

import static org.openremote.beta.shared.util.Util.*;

public class FlowServiceConfiguration extends DefaultConfiguration {

    private static final Logger LOG = Logger.getLogger(FlowServiceConfiguration.class.getName());

    class FlowServiceRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/flow")
                .get("")
                .route().id("GET all flows")
                .process(exchange -> {

                    // TODO: This is test/sample data
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

                    Flow[] flows = new Flow[1];
                    flows[0] = new Flow(
                        UUID.randomUUID().toString(),
                        "My Flow",
                        new Node[]{
                            httpRequest,
                            groovyTemplate,
                            httpResponse
                        },
                        new Wire[]{
                            new Wire(httpRequestSlot.getId(), groovyTemplateInSlot.getId()),
                            new Wire(groovyTemplateOutSlot.getId(), httpResponseSlot.getId())
                        }
                    );

                    exchange.getOut().setBody(flows);
                })
                .endRest();
        }
    }

    @Override
    public void apply(CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new FlowServiceRouteBuilder());
    }

}
