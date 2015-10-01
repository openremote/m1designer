package org.openremote.server.announce;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.openremote.server.web.UndertowService;

public class ControllerAnnounceConfiguration implements Configuration {

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        ControllerAnnounceService service = new ControllerAnnounceService(
            context.hasService(UndertowService.class)
        );
        context.addService(service);
    }
}
