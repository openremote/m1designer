/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
            LOG.info("--- Applying configuration: " + cfg.getClass().getName());
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