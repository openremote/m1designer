package org.openremote.beta.server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpHeaderFilterStrategy;
import org.apache.camel.model.rest.RestBindingMode;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.server.Environment.DEV_MODE;
import static org.openremote.beta.server.Environment.DEV_MODE_DEFAULT;

public class WebserverConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(WebserverConfiguration.class);

    public static final String WEBSERVER_ADDRESS = "WEBSERVER_ADDRESS";
    public static final String WEBSERVER_ADDRESS_DEFAULT = "127.0.0.1";
    public static final String WEBSERVER_PORT = "WEBSERVER_PORT";
    public static final String WEBSERVER_PORT_DEFAULT = "8080";
    public static final String WEBSERVER_DOCUMENT_ROOT = "WEBSERVER_DOCUMENT_ROOT";
    public static final String WEBSERVER_DOCUMENT_ROOT_DEFAULT = "src/main/webapp";
    public static final String WEBSERVER_DOCUMENT_CACHE_SECONDS = "WEBSERVER_DOCUMENT_CACHE_SECONDS";
    public static final String WEBSERVER_DOCUMENT_CACHE_SECONDS_DEFAULT = "300";
    public static final String WEBSERVER_ALLOW_ORIGIN = "WEBSERVER_ALLOW_ORIGIN";
    public static final String WEBSERVER_ALLOW_ORIGIN_DEFAULT = "*";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        environment.getRegistry().put("customHeaderFilterStrategy", new CustomHeaderFilterStrategy());

        boolean devMode = Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT));
        String host = environment.getProperty(WEBSERVER_ADDRESS, WEBSERVER_ADDRESS_DEFAULT);
        int port = Integer.valueOf(environment.getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT));
        String documentRoot = environment.getProperty(WEBSERVER_DOCUMENT_ROOT, WEBSERVER_DOCUMENT_ROOT_DEFAULT);
        int documentCacheSeconds = Integer.valueOf(environment.getProperty(WEBSERVER_DOCUMENT_CACHE_SECONDS, WEBSERVER_DOCUMENT_CACHE_SECONDS_DEFAULT));

        UndertowService undertowService = new UndertowService(devMode, host, port, documentRoot, documentCacheSeconds);
        context.addService(undertowService);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration()

                    .component("servlet")

                    .contextPath(UndertowService.SERVICE_CONTEXT_PATH).port(port)

                    .enableCORS(true)
                    .corsHeaderProperty(
                        "Access-Control-Allow-Origin",
                        environment.getProperty(WEBSERVER_ALLOW_ORIGIN, WEBSERVER_ALLOW_ORIGIN_DEFAULT)
                    )

                    .bindingMode(RestBindingMode.json)

                    .endpointProperty("headerFilterStrategy", "customHeaderFilterStrategy");

                from("direct:restStatusNotFound")
                    .id("REST Not Found Error Handler")
                    .choice().when(body().isNull()).setHeader(HTTP_RESPONSE_CODE, constant(404));
            }
        });
    }

    public static abstract class RestRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            onException(JsonProcessingException.class)
                .process(new JsonProcessingExceptionHandler())
                .handled(true);
        }
    }

    /**
     * This handles JSON unmarshalling errors when we receive JSON and can't convert it to POJO.
     */
    public static class JsonProcessingExceptionHandler implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            Throwable cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
            exchange.getOut().setBody(cause.getMessage());
            exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
            exchange.getContext().createProducerTemplate()
                .send("log:org.openremote.beta.json?level=WARN&showCaughtException=true&showBodyType=false&showExchangePattern=false", exchange);
        }
    }

    public static class CustomHeaderFilterStrategy extends HttpHeaderFilterStrategy {
        @Override
        protected void initialize() {
            super.initialize();

            // Why is this shit not filtered by default? Why would you ever send this back to an HTTP client?
            // TODO: What about all the general headers that can appear in requests AND response?!
            getOutFilter().add("accept");
            getOutFilter().add("accept-encoding");
            getOutFilter().add("accept-language");
            getOutFilter().add("dnt");
            getOutFilter().add("expect");
            getOutFilter().add("from");
            getOutFilter().add("if-match");
            getOutFilter().add("if-modified-since");
            getOutFilter().add("if-none-match");
            getOutFilter().add("if-range");
            getOutFilter().add("if-unmodified-since");
            getOutFilter().add("max-forwards");
            getOutFilter().add("proxy-authorization");
            getOutFilter().add("referer");
            getOutFilter().add("user-agent");
            getOutFilter().add("upgrade-insecure-requests");
            getOutFilter().add("vary");
        }
    }
}
