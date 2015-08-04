package org.openremote.beta.server;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;

public class ServerCamelContext extends DefaultCamelContext {

    @Override
    protected Registry createRegistry() {
        return new SimpleRegistry();
    }

}
