package org.openremote.client.shared.session.event;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.openremote.shared.event.Event;

public interface EventCodec extends JsonEncoderDecoder<Event> {}
