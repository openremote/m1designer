package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Slot extends FlowObject {

    public static final String TYPE_SINK = "urn:org-openremote:flow:slot:sink";
    public static final String TYPE_SOURCE = "urn:org-openremote:flow:slot:source";

    public boolean connectable = true;
    public String peerId;
    public String propertyPath;

    public Slot() {
        this(null);
    }

    public Slot(Identifier identifier) {
        super(null, identifier);
    }

    public Slot(String label, Identifier identifier) {
        this(label, identifier, null);
    }

    public Slot(String label, Identifier identifier, String propertyPath) {
        this(label, identifier, true, null, propertyPath);
    }

    public Slot(Identifier identifier, boolean connectable) {
        this(null, identifier, connectable);
    }

    public Slot(String label, Identifier identifier, boolean connectable) {
        this(label, identifier, connectable, null, null);
    }

    public Slot(String id, Slot peer, String label) {
        this(label, new Identifier(id, peer.getIdentifier().getType()), true, peer.getId(), null);
    }

    public Slot(String label, Identifier identifier, boolean connectable, String peerId, String propertyPath) {
        super(label, identifier);
        this.connectable = connectable;
        this.peerId = peerId;
        this.propertyPath = propertyPath;
    }

    public boolean isConnectable() {
        return connectable;
    }

    public void setConnectable(boolean connectable) {
        this.connectable = connectable;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "label='" + label + '\'' +
            ", id=" + identifier +
            ", connectable=" + connectable +
            ", peerId=" + peerId +
            ", propertyPath=" + propertyPath +
            '}';
    }
}
