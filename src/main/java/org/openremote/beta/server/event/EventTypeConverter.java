package org.openremote.beta.server.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Converter;
import org.openremote.beta.shared.event.FlowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class EventTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(EventTypeConverter.class);

    final static protected ObjectMapper JSON = new ObjectMapper();

    @Converter
    public static String writeFlowEvent(FlowEvent event) throws Exception {
        LOG.debug("Writing FlowEvent JSON: " + event);
        return JSON.writeValueAsString(event);
    }

    @Converter
    public static FlowEvent readFlowEvent(String string) throws Exception {
        LOG.debug("Reading FlowEvent JSON: " + string);
        return JSON.readValue(string, FlowEvent.class);
    }

}