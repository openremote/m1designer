package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Wire {

    public String sourceId;
    public String sinkId;

    public Wire() {
    }

    public Wire(Slot source, Slot sink) {
        this(source.getId(), sink.getId());
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
            && equalsSlotIds(sourceSlot.getId(), sinkSlot.getId());
    }

    public boolean equalsSlotIds(String sourceId, String sinkId) {
        return getSourceId().equals(sourceId) && getSinkId().equals(sinkId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wire wire = (Wire) o;

        if (sourceId != null ? !sourceId.equals(wire.sourceId) : wire.sourceId != null) return false;
        return !(sinkId != null ? !sinkId.equals(wire.sinkId) : wire.sinkId != null);
    }

    @Override
    public int hashCode() {
        int result = sourceId != null ? sourceId.hashCode() : 0;
        result = 31 * result + (sinkId != null ? sinkId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Wire{" +
            "sourceId='" + sourceId + '\'' +
            ", sinkId='" + sinkId + '\'' +
            '}';
    }
}