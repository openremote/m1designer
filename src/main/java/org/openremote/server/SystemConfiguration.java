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
import org.apache.camel.Processor;
import org.apache.camel.builder.LoggingErrorHandlerBuilder;
import org.apache.camel.impl.DefaultStreamCachingStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.StreamCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;

import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;

public class SystemConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        if (Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))) {
            LOG.info("######################## DEV MODE ########################");
        }

        // Java's herpes
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getGlobal().setLevel(Level.FINEST);

        // TODO make configurable in environment
        context.disableJMX();

        // TODO might need this for errorhandler
        context.setAllowUseOriginalMessage(false);

        // Don't use JMS, we do our own correlation
        context.setUseBreadcrumb(false);

        // TODO: Wait 5 seconds before forcing a route to stop?
        context.getShutdownStrategy().setTimeout(5);

        context.setStreamCaching(true);
        StreamCachingStrategy streamCachingStrategy = new DefaultStreamCachingStrategy();
        streamCachingStrategy.setSpoolThreshold(524288); // Half megabyte
        context.setStreamCachingStrategy(streamCachingStrategy);

        context.setErrorHandlerBuilder(new LoggingErrorHandlerBuilder() {
            @Override
            public Processor createErrorHandler(RouteContext routeContext, Processor processor) {
                return super.createErrorHandler(routeContext, processor);
            }
        });

    }
}
