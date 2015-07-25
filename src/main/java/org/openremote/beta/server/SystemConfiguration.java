package org.openremote.beta.server;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultStreamCachingStrategy;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.StreamCachingStrategy;

public class SystemConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        // TODO make configurable in environment
        context.disableJMX();

        context.setAllowUseOriginalMessage(false);

        context.setStreamCaching(true);
        StreamCachingStrategy streamCachingStrategy = new DefaultStreamCachingStrategy();
        streamCachingStrategy.setSpoolThreshold(524288); // Half megabyte
        context.setStreamCachingStrategy(streamCachingStrategy);
    }
}
