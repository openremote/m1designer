package org.openremote.beta.server.announce;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.web.UndertowService;

public class ControllerAnnounceConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        ControllerAnnounceService service = new ControllerAnnounceService(
            context.hasService(UndertowService.class)
        );
        context.addService(service);
    }
}
