package org.openremote.beta.server;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.LoggingErrorHandlerBuilder;
import org.apache.camel.impl.DefaultStreamCachingStrategy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.StreamCachingStrategy;

public class SystemConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        // TODO make configurable in environment
        context.disableJMX();

        // TODO might need this for errorhandler
        context.setAllowUseOriginalMessage(false);

        // Don't use JMS, we do our own correlation
        context.setUseBreadcrumb(false);

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
