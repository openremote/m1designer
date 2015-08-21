package org.openremote.beta.client.shared.session;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.NonBubblingEvent;
import org.openremote.beta.shared.event.Event;

@JsExport
@JsType
public class SessionCloseEvent extends Event implements NonBubblingEvent {
}
