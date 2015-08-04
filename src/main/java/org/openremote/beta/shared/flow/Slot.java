package org.openremote.beta.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Slot extends FlowObject {

    public static final String TYPE_SINK = "urn:org-openremote:flow:slot:sink";
    public static final String TYPE_SOURCE = "urn:org-openremote:flow:slot:source";

    public boolean visible = true;
    public Identifier peerIdentifier;

    public Slot() {
    }

    public Slot(Identifier identifier) {
        super(null, identifier);
    }

    public Slot(String label, Identifier identifier) {
        super(label, identifier);
    }

    public Slot(Identifier identifier, boolean visible) {
        this(null, identifier, visible);
    }

    public Slot(String label, Identifier identifier, boolean visible) {
        super(label, identifier);
        this.visible = visible;
    }

    public Slot(String label, Identifier identifier, Identifier peerIdentifier) {
        this(label, identifier);
        this.peerIdentifier = peerIdentifier;
    }

    public Slot(String id, Slot peer) {
        this(peer.getLabel(), new Identifier(id, peer.getIdentifier().getType()), peer.getIdentifier());
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Identifier getPeerIdentifier() {
        return peerIdentifier;
    }

    public void setPeerIdentifier(Identifier peerIdentifier) {
        this.peerIdentifier = peerIdentifier;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "label='" + label + '\'' +
            ", id=" + identifier +
            ", visible=" + visible +
            ", peerIdentifier=" + peerIdentifier +
            '}';
    }
}
