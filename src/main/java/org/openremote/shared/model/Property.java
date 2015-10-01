package org.openremote.shared.model;

public class Property<T> {

    public PropertyDescriptor<T> descriptor;
    public T value;

    protected Property() {
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
