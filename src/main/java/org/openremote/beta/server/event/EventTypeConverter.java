package org.openremote.beta.server.event;

import org.apache.camel.Converter;
import org.openremote.beta.server.util.JsonUtil;
import org.openremote.beta.shared.event.FlowIdEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter
public class EventTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(EventTypeConverter.class);

    @Converter
    public static String writeFlowEvent(FlowIdEvent event) throws Exception {
        LOG.trace("Writing JSON: " + event);
        return JsonUtil.JSON.writeValueAsString(event);
    }

    @Converter
    public static FlowIdEvent readFlowEvent(String string) throws Exception {
        LOG.trace("Reading FlowEvent JSON: " + string);
        return JsonUtil.JSON.readValue(string, FlowIdEvent.class);
    }

    @Converter
    public static String writeMessageEvent(MessageEvent event) throws Exception {
        LOG.trace("Writing JSON: " + event);
        return JsonUtil.JSON.writeValueAsString(event);
    }

    @Converter
    public static MessageEvent readMessageEvent(String string) throws Exception {
        LOG.trace("Reading MessageEvent JSON: " + string);
        return JsonUtil.JSON.readValue(string, MessageEvent.class);
    }
}