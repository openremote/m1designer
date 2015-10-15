package org.openremote.client.event;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.openremote.shared.event.Event;

public interface EventCodec extends JsonEncoderDecoder<Event> {}
