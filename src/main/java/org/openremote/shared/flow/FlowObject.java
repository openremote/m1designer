package org.openremote.shared.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.model.Identifier;

@JsType
public class FlowObject {

    public String label;

    public Identifier identifier;

    protected FlowObject() {
    }

    public FlowObject(String label, Identifier identifier) {
        this.label = label;
        this.identifier = identifier;
    }

    public boolean isLabelEmpty() {
        return getLabel() == null || getLabel().length() == 0;
    }

    public String getDefaultedLabel() {
        return isLabelEmpty() ? "Unnamed " + getClass().getSimpleName(): getLabel();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowObject that = (FlowObject) o;

        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "label='" + label + '\'' +
            ", id=" + identifier +
            '}';
    }

}
