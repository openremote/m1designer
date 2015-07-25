package org.openremote.beta.shared.inventory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Property {

    public enum Type {
        STRING,
        BOOLEAN,
        NUMBER;
    }

    protected String label;
    protected String description;
    protected Type type = Type.STRING;
    protected String defaultValue;
    protected String defaultValueNote;
    protected String value;

    public Property() {
    }

    public Property(String label, String description, Type type) {
        this.label = label;
        this.description = description;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Number getNumberValue() {
        return getValue() != null ? Integer.valueOf(getValue()) : null;
    }

    public void setNumberValue(Number value) {
        setValue(value != null ? value.toString() : null);
    }

    public Boolean getBooleanValue() {
        return getValue() != null ? Boolean.valueOf(getValue()) : null;
    }

    public void setBooleanValue(Boolean value) {
        setValue(value != null ? value.toString() : null);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValueNote() {
        return defaultValueNote;
    }

    public void setDefaultValueNote(String defaultValueNote) {
        this.defaultValueNote = defaultValueNote;
    }

    @Override
    public String toString() {
        return "Property{" +
            "label='" + label + '\'' +
            ", type=" + type +
            ", value='" + value + '\'' +
            '}';
    }
}
