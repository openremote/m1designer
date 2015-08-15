package org.openremote.beta.shared.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Property {

    public enum Type {
        STRING,
        BOOLEAN,
        NUMBER;
    }

    protected String label;
    protected String description;
    protected Type type = Type.STRING;
    protected boolean required;
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

    public Property setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Property setDescription(String description) {
        this.description = description;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Property setType(Type type) {
        this.type = type;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public Property setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean hasValue() {
        return value != null;
    }

    public String getValue() {
        return value;
    }

    public Property setValue(String value) {
        this.value = value;
        return this;
    }

    public Number getNumberValue() {
        return hasValue() ? Integer.valueOf(getValue()) : null;
    }

    public Property setNumberValue(Number value) {
        return setValue(value != null ? value.toString() : null);
    }

    public Boolean getBooleanValue() {
        return hasValue() ? Boolean.valueOf(getValue()) : null;
    }

    public Property setBooleanValue(Boolean value) {
        return setValue(value != null ? value.toString() : null);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Property setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getDefaultValueNote() {
        return defaultValueNote;
    }

    public Property setDefaultValueNote(String defaultValueNote) {
        this.defaultValueNote = defaultValueNote;
        return this;
    }

    @Override
    public String toString() {
        return "Property{" +
            "label='" + label + '\'' +
            ", type=" + type +
            ", required=" + required +
            ", value='" + value + '\'' +
            '}';
    }
}
