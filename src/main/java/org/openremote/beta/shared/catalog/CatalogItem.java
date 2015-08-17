package org.openremote.beta.shared.catalog;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.flow.NodeColor;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include= NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class CatalogItem {

    public String label;

    public CatalogCategory category;

    public String nodeType;

    public NodeColor nodeColor;

    protected CatalogItem() {
    }

    public CatalogItem(String label, CatalogCategory category, String nodeType, NodeColor nodeColor) {
        this.label = label;
        this.category = category;
        this.nodeType = nodeType;
        this.nodeColor = nodeColor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CatalogCategory getCategory() {
        return category;
    }

    public void setCategory(CatalogCategory category) {
        this.category = category;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public NodeColor getNodeColor() {
        return nodeColor;
    }

    public void setNodeColor(NodeColor nodeColor) {
        this.nodeColor = nodeColor;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "label='" + label + '\'' +
            ", category=" + category +
            ", nodeType=" + nodeType +
            '}';
    }

}
