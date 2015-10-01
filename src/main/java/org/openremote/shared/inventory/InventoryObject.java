package org.openremote.shared.inventory;

import org.openremote.shared.model.Identifier;
import org.openremote.shared.model.Property;

import java.util.*;

public class InventoryObject {

    public String label;

    public Identifier identifier;

    public Identifier[] keys = new Identifier[0];

    public Map<String, Property> properties = new LinkedHashMap<>();

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

    public String getId() {
        return getIdentifier().getId();
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

    public Map<String, Property> getProperties() {
        return properties;
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
            ", properties=" + properties +
            '}';
    }
}
