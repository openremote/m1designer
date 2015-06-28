package org.openremote.beta.server;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.SimpleRegistry;

public interface Configuration {

    void apply(SimpleRegistry simpleRegistry) throws Exception ;

    void apply(CamelContext camelContext) throws Exception ;

}
