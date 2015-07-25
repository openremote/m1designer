package org.openremote.beta.server;

import org.apache.camel.CamelContext;

public interface Configuration {

    void apply(Environment environment, CamelContext context) throws Exception ;

}
