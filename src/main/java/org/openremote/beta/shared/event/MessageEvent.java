package org.openremote.beta.shared.event;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.Slot;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsExport
@JsType
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class MessageEvent extends Event {

    public String sinkSlotId;
    public String instanceId;
    public Map<String, Object> headers;
    public String body;

    @JsNoExport
    public MessageEvent() {
        this(null, null, null, null);
    }

    @JsNoExport
    public MessageEvent(Slot sinkSlot, String body) {
        this(sinkSlot.getId(), null, body, null);
    }

    @JsNoExport
    public MessageEvent(Slot sinkSlot, String instanceId, String body) {
        this(sinkSlot.getId(), instanceId, body, null);
    }

    public MessageEvent(String sinkSlotId, String instanceId, String body, Map<String, Object> headers) {
        this.sinkSlotId = sinkSlotId;
        this.instanceId = instanceId;
        this.body = body;
        this.headers = headers;
    }

    public String getSinkSlotId() {
        return sinkSlotId;
    }

    public void setSinkSlotId(String sinkSlotId) {
        this.sinkSlotId = sinkSlotId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public boolean hasHeaders() {
        return getHeaders() != null;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
            ", sinkSlotId='" + sinkSlotId + '\'' +
            ", instanceId='" + instanceId + '\'' +
            ", headers=" + headers +
            ", body='" + body + '\'' +
            '}';
    }
}
