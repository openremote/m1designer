package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;

@JsType
public class Wire {

    public String sourceId;
    public String sinkId;

    public Wire() {
    }

    public Wire(String sourceId, String sinkId) {
        this.sourceId = sourceId;
        this.sinkId = sinkId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSinkId() {
        return sinkId;
    }

    public boolean equalsSlots(Slot sourceSlot, Slot sinkSlot) {
        return !(sourceSlot == null || sinkSlot == null)
            && equals(sourceSlot.getId(), sinkSlot.getId());
    }

    public boolean equals(String sourceId, String sinkId) {
        return getSourceId().equals(sourceId) && getSinkId().equals(sinkId);
    }

    @Override
    public String toString() {
        return "Wire{" +
            "sourceId='" + sourceId + '\'' +
            ", sinkId='" + sinkId + '\'' +
            '}';
    }
}