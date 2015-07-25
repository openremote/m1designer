package org.openremote.beta.server;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

import static org.openremote.beta.server.Environment.DEV_MODE;
import static org.openremote.beta.server.Environment.DEV_MODE_DEFAULT;

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
            LOG.info("Applying configuration: " + cfg.getClass().getName());
            cfg.apply(environment, context);
        }
    }

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext() {
            @Override
            protected Registry createRegistry() {
                return new SimpleRegistry();
            }
        };
        Server server = new Server(context);
        if (Boolean.valueOf(server.environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))) {
            LOG.info("######################## DEV MODE ########################");
        }
        context.start();
    }
}