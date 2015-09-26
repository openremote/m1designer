package org.openremote.beta.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import gumi.builders.UrlBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.MessageHistory;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.Server;
import org.openremote.beta.server.SystemConfiguration;
import org.openremote.beta.server.catalog.CatalogServiceConfiguration;
import org.openremote.beta.server.catalog.NodeDescriptorConfiguration;
import org.openremote.beta.server.event.EventServiceConfiguration;
import org.openremote.beta.server.event.WebSocketEventServiceConfiguration;
import org.openremote.beta.server.flow.FlowServiceConfiguration;
import org.openremote.beta.server.inventory.InventoryServiceConfiguration;
import org.openremote.beta.server.route.RouteManagementServiceConfiguration;
import org.openremote.beta.server.util.JsonUtil;
import org.openremote.beta.server.web.WebserverConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.openremote.beta.server.util.UrlUtil.url;
import static org.openremote.beta.shared.Constants.REST_SERVICE_CONTEXT_PATH;
import static org.openremote.beta.shared.Constants.WEBSOCKET_SERVICE_CONTEXT_PATH;

public class IntegrationTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

    protected Server server;
    protected Environment environment;
    protected String serverEphemeralPort;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        LOG.info("Configuring integration test environment/server");

        CamelContext context = new DefaultCamelContext(new SimpleRegistry());

        context.setTracing(isTracingEnabled());

        Properties properties = new Properties();
        List<Configuration> configurations = new ArrayList<>();

        serverEphemeralPort = findEphemeralPort();

        configure(properties, configurations, context);

        environment = new Environment(context, false, properties);
        server = new Server(environment, context, configurations);

        return context;
    }

    protected void configure(Properties properties,
                             List<Configuration> configurations,
                             CamelContext context) throws Exception {

        configurations.add(new SystemConfiguration());

        properties.put(WebserverConfiguration.WEBSERVER_ADDRESS, getServerHost());
        properties.put(WebserverConfiguration.WEBSERVER_PORT, getServerPort());
        configurations.add(new WebserverConfiguration());

        configurations.add(new NodeDescriptorConfiguration());
        configurations.add(new CatalogServiceConfiguration());
        configurations.add(new InventoryServiceConfiguration());
        configurations.add(new RouteManagementServiceConfiguration());
        configurations.add(new FlowServiceConfiguration());
        configurations.add(new EventServiceConfiguration());

        configurations.add(new WebSocketEventServiceConfiguration());

    }

    @Override
    protected int getShutdownTimeout() {
        return 2;
    }

    protected boolean isTracingEnabled() {
        return false;
    }

    protected ObjectMapper createJsonMapper() {
        return JsonUtil.JSON;
    }

    protected Environment environment() {
        return environment;
    }

    protected String getWebServerScheme() {
        return "http";
    }

    protected String getWebSocketScheme() {
        return "ahc-ws";
    }

    protected String getServerHost() {
        return "127.0.0.1";
    }

    protected String getServerPort() {
        return serverEphemeralPort;
    }

    protected String restClientUrl(String... pathSegments) {
        return restClientUrlBuilder(pathSegments).toString();
    }

    protected UrlBuilder restClientUrlBuilder(String... pathSegments) {
        return url(getWebServerScheme(), getServerHost(), getServerPort(), REST_SERVICE_CONTEXT_PATH, pathSegments)
            // Throw exception on failure, don't swallow OUT message with status code > 400
            .addParameter("throwExceptionOnFailure", "false");
    }

    protected String websocketClientUrl(String... pathSegments) {
        return websocketClientUrlBuilder(pathSegments).toString();
    }

    protected UrlBuilder websocketClientUrlBuilder(String... pathSegments) {
        return url(getWebSocketScheme(), getServerHost(), getServerPort(), WEBSOCKET_SERVICE_CONTEXT_PATH, pathSegments);
    }

    protected <T> T fromJson(String json, Class<T> type) throws Exception {
        return JsonUtil.JSON.readValue(json, type);
    }

    protected String toJson(Object o) throws Exception {
        return JsonUtil.JSON.writeValueAsString(o);
    }

    protected void logExchangeHistory(Exchange exchange) {
        List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
        LOG.info("################################ HISTORY OF: " + exchange);
        for (MessageHistory messageHistory : list) {
            if (messageHistory.getNode().getId().startsWith("log"))
                continue;
            LOG.info("------------------------------------------------------------------------------------------------");
            LOG.info("### ROUTE               : " + messageHistory.getRouteId());
            LOG.info("### ROUTE DESCRIPTION   : " + context().getRoute(messageHistory.getRouteId()).getDescription());
            LOG.info("### PROCESSOR ID        : " + messageHistory.getNode().getId());
        }
    }

    private String findEphemeralPort() {
        // TODO: As usual it's impossible with the Camel API to use an ephemeral port and then get that port somehow...
        try {
            ServerSocket socket = new ServerSocket(0, 0, Inet4Address.getLocalHost());
            int port = socket.getLocalPort();
            socket.close();
            // Yeah, that port might be occupied by something else now, but what do you want me to do?
            return Integer.toString(port);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

}