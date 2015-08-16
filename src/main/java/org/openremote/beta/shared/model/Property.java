package org.openremote.beta.shared.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class Property<T> {

    public PropertyDescriptor<T> descriptor;
    public T value;

    public Property() {
    }

    public Property(PropertyDescriptor<T> descriptor, T value) {
        this.descriptor = descriptor;
        this.value = value;
    }

    public T getValue() {
        return value != null ? getDescriptor().read(value) : null;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public PropertyDescriptor<T> getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(PropertyDescriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "Property{" +
            "descriptor=" + descriptor +
            ", value=" + value +
            '}';
    }
}
