package org.openremote.shared.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes({
    @JsonSubTypes.Type(value = PropertyDescriptor.StringType.class, name = "STRING"),
    @JsonSubTypes.Type(value = PropertyDescriptor.LongType.class, name="LONG"),
    @JsonSubTypes.Type(value = PropertyDescriptor.IntegerType.class, name = "INTEGER"),
    @JsonSubTypes.Type(value = PropertyDescriptor.DoubleType.class, name = "DOUBLE"),
    @JsonSubTypes.Type(value = PropertyDescriptor.BooleanType.class, name = "BOOLEAN"),
})
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
public abstract class PropertyDescriptor<T> {

    public static final PropertyDescriptor<String> TYPE_STRING = new StringType();
    public static final PropertyDescriptor<Long> TYPE_LONG = new LongType();
    public static final PropertyDescriptor<Integer> TYPE_INTEGER = new IntegerType();
    public static final PropertyDescriptor<Double> TYPE_DOUBLE = new DoubleType();
    public static final PropertyDescriptor<Boolean> TYPE_BOOLEAN = new BooleanType();

    public static class StringType extends PropertyDescriptor<String> {

        public StringType() {
        }

        public StringType(String label, String description) {
            super(label, description);
        }

        @Override
        public String read(Object value) {
            return value.toString();
        }
    }

    public static class LongType extends PropertyDescriptor<Long> {

        public LongType() {
        }

        public LongType(String label, String description) {
            super(label, description);
        }

        @Override
        public Long read(Object value) {
            return Long.valueOf(value.toString());
        }
    }

    public static class IntegerType extends PropertyDescriptor<Integer> {

        public IntegerType() {
        }

        public IntegerType(String label, String description) {
            super(label, description);
        }

        @Override
        public Integer read(Object value) {
            return Integer.valueOf(value.toString());
        }
    }

    public static class DoubleType extends PropertyDescriptor<Double> {

        public DoubleType() {
        }

        public DoubleType(String label, String description) {
            super(label, description);
        }

        @Override
        public Double read(Object value) {
            return Double.valueOf(value.toString());
        }
    }

    public static class BooleanType extends PropertyDescriptor<Boolean> {

        public BooleanType() {
        }

        public BooleanType(String label, String description) {
            super(label, description);
        }

        @Override
        public Boolean read(Object value) {
            return Boolean.valueOf(value.toString());
        }
    }

    protected String label;
    protected String description;
    protected boolean required;
    protected String defaultValue;
    protected String defaultValueNote;

    protected PropertyDescriptor() {
    }

    protected PropertyDescriptor(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public PropertyDescriptor setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PropertyDescriptor setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public PropertyDescriptor setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public PropertyDescriptor setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getDefaultValueNote() {
        return defaultValueNote;
    }

    public PropertyDescriptor setDefaultValueNote(String defaultValueNote) {
        this.defaultValueNote = defaultValueNote;
        return this;
    }

    public abstract T read(Object value);

    @Override
    public String toString() {
        return getClass().getName() + "{" +
            "label='" + label + '\'' +
            ", required=" + required +
            '}';
    }
}
