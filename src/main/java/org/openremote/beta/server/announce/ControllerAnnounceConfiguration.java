package org.openremote.beta.server.announce;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;

import static org.openremote.beta.server.WebserverConfiguration.*;

public class ControllerAnnounceConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        String presentationHost = environment.getProperty(WEBSERVER_ADDRESS, WEBSERVER_ADDRESS_DEFAULT);
        String presentationPort = environment.getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT);

        ControllerAnnounceService service = new ControllerAnnounceService(presentationHost, presentationPort);
        context.addService(service);
    }
}
