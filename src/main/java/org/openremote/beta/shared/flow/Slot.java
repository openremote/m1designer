package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;

@JsType
public class Slot {

    @JsType
    public enum Type {
        SINK,
        SOURCE
    }

    public String id;
    public Type type;
    public String label;

    public Slot() {
    }

    public Slot(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Slot(String id, Type type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Slot{" +
            "id='" + id + '\'' +
            ", type=" + type +
            ", label='" + label + '\'' +
            '}';
    }
}
