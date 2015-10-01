package org.openremote.shared.model;

import com.google.gwt.core.client.js.JsType;

@JsType
public class Identifier {

    public String id;
    public String type; // URI

    protected Identifier() {
    }

    public Identifier(String id) {
        this.id = id;
    }

    public Identifier(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return type + ':' + id;
    }
}
