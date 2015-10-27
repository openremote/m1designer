package org.openremote.server;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    static {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                System.err.println("In thread '" + t + "', uncaught exception: " + e);
                e.printStackTrace(System.err);
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    final protected Environment environment;

    public Server(CamelContext context) throws Exception {
        this(new Environment(context), context, ServiceLoader.load(Configuration.class));
    }

    public Server(Environment environment, CamelContext context, Iterable<Configuration> configurations) throws Exception {
        this.environment = environment;
        for (Configuration cfg : configurations) {
            LOG.info("> Applying configuration: " + cfg.getClass().getName());
            cfg.apply(environment, context);
        }
    }

    public static void main(String[] args) throws Exception {
        LOG.info("Starting server...");
        CamelContext context = new ServerCamelContext();
        new Server(context);
        LOG.info("Starting CamelContext...");
        context.start();
        LOG.info("Server ready");
    }
}