package org.openremote.beta.server;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.openremote.beta.server.util.LoggingUtil;

import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.openremote.beta.server.Environment.DEV_MODE;

public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    public static final DefaultCamelContext CONTEXT;

    static {
        try {
            LoggingUtil.loadDefaultConfiguration();
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                System.err.println("In thread '" + t + "', uncaught exception: " + e);
                e.printStackTrace(System.err);
            });
            CONTEXT = new DefaultCamelContext();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws Exception {

        if (Boolean.valueOf(Environment.get(DEV_MODE))) {
            LOG.info("######################## DEV MODE ########################");
        }

        SimpleRegistry registry = new SimpleRegistry();
        CONTEXT.setRegistry(registry);

        ServiceLoader<Configuration> configurations = ServiceLoader.load(Configuration.class);
        for (Configuration cfg : configurations) {
            LOG.info("Applying configuration: " + cfg.getClass().getName());
            cfg.apply(registry);
            cfg.apply(CONTEXT);
        }

        CONTEXT.start();

        try {
            new CountDownLatch(1).await(-1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            System.exit(0);
        }
    }
}