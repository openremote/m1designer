package org.openremote.beta.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.openremote.beta.server.*;
import org.openremote.beta.server.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class IntegrationTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

    protected ObjectMapper jsonMapper;

    protected Server server;
    protected Environment environment;
    protected String ephemeralPort;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        LOG.info("Configuring integration test environment/server");

        CamelContext context = new DefaultCamelContext(new SimpleRegistry());

        Properties properties = new Properties();
        List<Configuration> configurations = new ArrayList<>();

        ephemeralPort = findEphemeralPort();
        configure(properties, configurations, context);

        environment = new Environment(context, false, properties);
        server = new Server(environment, context, configurations);

        jsonMapper = createJsonMapper();

        return context;
    }

    protected void configure(Properties properties,
                             List<Configuration> configurations,
                             CamelContext context) throws Exception {

        configurations.add(new SystemConfiguration());

        properties.put(WebserverConfiguration.WEBSERVER_ADDRESS, getServerHost());
        properties.put(WebserverConfiguration.WEBSERVER_PORT, getServerPort());
        configurations.add(new WebserverConfiguration());
    }

    protected ObjectMapper createJsonMapper() {
        return JsonUtil.JSON;
    }

    protected Environment environment() {
        return environment;
    }

    protected String getServerScheme() {
        return "http";
    }

    protected String getServerHost() {
        return "127.0.0.1";
    }

    protected String getServerPort() {
        return ephemeralPort;
    }

    protected String createHttpUri(String... pathSegments) {
        StringBuilder path = new StringBuilder();
        if (pathSegments != null) {
            for (String pathSegment : pathSegments) {
                path.append(pathSegment.startsWith("/") ? "" : "/").append(pathSegment);
            }
        }
        try {
            URI uri = new URI(getServerScheme(), null, getServerHost(), Integer.valueOf(getServerPort()), path.toString(), null, null);
            return uri.toString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected <T> T fromJson(String json, Class<T> type) throws Exception {
        return jsonMapper.readValue(json, type);
    }

    protected String toJson(Object o) throws Exception {
        return jsonMapper.writeValueAsString(o);
    }

    private String findEphemeralPort() {
        // TODO: As usual it's impossible with the Camel API to let Jetty use an ephemeral port and then get that port somehow...
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