package org.openremote.beta.server;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpHeaderFilterStrategy;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.rest.RestBindingMode;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.openremote.beta.server.Environment.*;

public class WebserverConfiguration extends DefaultConfiguration {

    private static final Logger LOG = Logger.getLogger(WebserverConfiguration.class.getName());

    @Override
    public void apply(SimpleRegistry registry) throws Exception {

        ResourceHandler staticResourcesHandler = new ResourceHandler() {
            @Override
            protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
                super.doResponseHeaders(response, resource, mimeType);
                if (resource.getName().contains("nocache")) {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
                } else {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, Environment.get(WEBSERVER_DEFAULT_CACHE_CONTROL));
                }
            }
        };

        Resource documentRoot = Resource.newResource(Environment.get(WEBSERVER_DOCUMENT_ROOT));
        LOG.info("Static document root: " + documentRoot);
        staticResourcesHandler.setBaseResource(documentRoot);
        registry.put("staticResourcesHandler", staticResourcesHandler);

        registry.put("customHeaderFilterStrategy", new CustomHeaderFilterStrategy());
    }

    @Override
    public void apply(CamelContext camelContext) throws Exception {

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration()

                    .component("jetty")

                    .host(Environment.get(WEBSERVER_ADDRESS))
                    .port(Environment.get(WEBSERVER_PORT))

                    .enableCORS(true)
                    .corsHeaderProperty("Access-Control-Allow-Origin", Environment.get(WEBSERVER_ALLOW_ORIGIN))

                    .bindingMode(RestBindingMode.json)

                    .endpointProperty("headerFilterStrategy", "customHeaderFilterStrategy")
                    .endpointProperty("handlers", "staticResourcesHandler");


                JettyHttpComponent jettyComponent = (JettyHttpComponent) getContext().getComponent("jetty");
                jettyComponent.setErrorHandler(new CustomErrorHandler());

            }
        });
    }

    public static class CustomErrorHandler extends ErrorHandler {
        // Override Jetty's awful error page with sourceHandle JSON entity
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            String reason = (response instanceof Response) ? ((Response) response).getReason() : null;
            if (reason == null)
                reason = HttpStatus.getMessage(response.getStatus());

            baseRequest.setHandled(true);

            ObjectNode errorObject = JsonNodeFactory.instance.objectNode();
            errorObject.set("status", new IntNode(response.getStatus()));
            errorObject.set("reason", new TextNode(reason));

            ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(4096);
            writer.write(errorObject.toString());
            writer.flush();

            response.setContentType("application/json");
            response.setContentLength(writer.size());
            writer.writeTo(response.getOutputStream());
            writer.destroy();
        }
    }

    public static class CustomHeaderFilterStrategy extends HttpHeaderFilterStrategy {
        @Override
        protected void initialize() {
            super.initialize();
            // Why would you not let me set cache control headers? This makes no sense...
            getOutFilter().remove("cache-control");
        }
    }
}
