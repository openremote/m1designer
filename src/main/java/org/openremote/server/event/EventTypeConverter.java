package org.openremote.server.event;

import org.apache.camel.Converter;
import org.openremote.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.server.util.JsonUtil.JSON;

@Converter
public class EventTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(EventTypeConverter.class);

    @Converter
    public static String writeEvent(Event event) throws Exception {
        LOG.trace("Writing event JSON: " + event);
        return JSON.writeValueAsString(event);
    }

    @Converter
    public static Event readEvent(String string) throws Exception {
        LOG.trace("Reading event JSON: " + string);
        return JSON.readValue(string, Event.class);
    }
}