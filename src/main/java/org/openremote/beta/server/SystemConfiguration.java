package org.openremote.beta.server;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultStreamCachingStrategy;
import org.apache.camel.spi.StreamCachingStrategy;

public class SystemConfiguration extends DefaultConfiguration {

    @Override
    public void apply(CamelContext camelContext) throws Exception {

        camelContext.disableJMX();

        camelContext.setAllowUseOriginalMessage(false);

        camelContext.setStreamCaching(true);
        StreamCachingStrategy streamCachingStrategy = new DefaultStreamCachingStrategy();
        streamCachingStrategy.setSpoolThreshold(524288); // Half megabyte
        camelContext.setStreamCachingStrategy(streamCachingStrategy);
    }
}
