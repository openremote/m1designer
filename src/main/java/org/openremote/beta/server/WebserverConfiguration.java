package org.openremote.beta.server;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpHeaderFilterStrategy;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.openremote.beta.server.Environment.DEV_MODE;
import static org.openremote.beta.server.Environment.DEV_MODE_DEFAULT;

public class WebserverConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebserverConfiguration.class);

    public static final String WEBSERVER_ADDRESS = "WEBSERVER_ADDRESS";
    public static final String WEBSERVER_ADDRESS_DEFAULT = "0.0.0.0";
    public static final String WEBSERVER_PORT = "WEBSERVER_PORT";
    public static final String WEBSERVER_PORT_DEFAULT = "8080";
    public static final String WEBSERVER_DOCUMENT_ROOT = "WEBSERVER_DOCUMENT_ROOT";
    public static final String WEBSERVER_DOCUMENT_ROOT_DEFAULT = "src/main/webapp";
    public static final String WEBSERVER_DEFAULT_CACHE_CONTROL = "WEBSERVER_DEFAULT_CACHE_CONTROL";
    public static final String WEBSERVER_DEFAULT_CACHE_CONTROL_DEFAULT = "max-age=300, must-revalidate";
    public static final String WEBSERVER_ALLOW_ORIGIN = "WEBSERVER_ALLOW_ORIGIN";
    public static final String WEBSERVER_ALLOW_ORIGIN_DEFAULT = "*";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        ResourceHandler staticResourcesHandler = new ResourceHandler() {
            @Override
            protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
                super.doResponseHeaders(response, resource, mimeType);
                if (resource.getName().contains("nocache")
                    || Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))) {
                    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
                } else {
                    response.setHeader(
                        HttpHeaders.CACHE_CONTROL,
                        environment.getProperty(WEBSERVER_DEFAULT_CACHE_CONTROL, WEBSERVER_DEFAULT_CACHE_CONTROL_DEFAULT)
                    );
                }
            }
        };

        Resource documentRoot = Resource.newResource(
            environment.getProperty(WEBSERVER_DOCUMENT_ROOT, WEBSERVER_DOCUMENT_ROOT_DEFAULT)
        );
        LOG.info("Static document root: " + documentRoot);
        staticResourcesHandler.setBaseResource(documentRoot);
        environment.getRegistry().put("staticResourcesHandler", staticResourcesHandler);

        environment.getRegistry().put("customHeaderFilterStrategy", new CustomHeaderFilterStrategy());

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration()

                    .component("jetty")

                    .host(environment.getProperty(WEBSERVER_ADDRESS, WEBSERVER_ADDRESS_DEFAULT))
                    .port(environment.getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT))

                    .enableCORS(true)
                    .corsHeaderProperty(
                        "Access-Control-Allow-Origin",
                        environment.getProperty(WEBSERVER_ALLOW_ORIGIN, WEBSERVER_ALLOW_ORIGIN_DEFAULT)
                    )

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
