package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class NodeDescriptorConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDescriptorConfiguration.class);

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        Iterable<NodeDescriptor> discoveredNodeDescriptors =
            ServiceLoader.load(NodeDescriptor.class);

        for (NodeDescriptor descriptor : discoveredNodeDescriptors) {
            LOG.info("Discovered node descriptor: " + descriptor.getType());
            if (environment.getRegistry().containsKey(descriptor.getType())) {
                throw new IllegalStateException("Duplicate node descriptor type: " + descriptor.getType());
            }
            environment.getRegistry().put(descriptor.getType(), descriptor);
        }
    }

}
