package org.openremote.beta.server.util;

import org.apache.camel.component.jackson.JacksonDataFormat;

public class CustomJacksonDataFormat extends JacksonDataFormat{

    public CustomJacksonDataFormat() {
        setEnableJaxbAnnotationModule(false);
        JsonUtil.configure(getObjectMapper());
    }
}
