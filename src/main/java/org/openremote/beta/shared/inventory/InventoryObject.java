package org.openremote.beta.shared.inventory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.beta.shared.model.Identifier;
import org.openremote.beta.shared.model.Property;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class InventoryObject {

    public String label;

    public Identifier identifier;

    public Identifier[] keys = new Identifier[0];

    public Map<String, Property> extra = new LinkedHashMap<>();

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

    public boolean isOfType(String type) {
        return getIdentifier().getType().equals(type);
    }

    public Identifier[] getKeys() {
        return keys;
    }

    public Identifier removeKey(Identifier identifier) {
        List<Identifier> list = new ArrayList<>(Arrays.asList(keys));
        boolean removed = list.remove(identifier);
        keys = list.toArray(new Identifier[keys.length]);
        return removed ? identifier : null;
    }

    public void addKey(Identifier identifier) {
        List<Identifier> list = new ArrayList<>(Arrays.asList(keys));
        list.add(identifier);
        keys = list.toArray(new Identifier[keys.length]);
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
