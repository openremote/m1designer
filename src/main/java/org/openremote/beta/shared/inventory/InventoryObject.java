package org.openremote.beta.shared.inventory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class InventoryObject {

    protected String label;

    protected Identifier identifier;

    protected Identifier[] keys = new Identifier[0];

    protected Map<String, Property> extra = new LinkedHashMap<>();

    protected InventoryObject() {
    }

    public InventoryObject(String label, Identifier identifier, Identifier... keys) {
        this.label = label;
        this.identifier = identifier;
        this.keys = keys;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Identifier[] getKeys() {
        return keys;
    }

    public Map<String, Property> getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryObject that = (InventoryObject) o;

        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "InventoryObject{" +
            "label='" + label + '\'' +
            ", id=" + identifier +
            ", keys=" + Arrays.toString(keys) +
            ", extra=" + extra +
            '}';
    }
}
