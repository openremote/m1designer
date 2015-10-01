package org.openremote.shared.flow;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gwt.core.client.js.JsType;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

@JsType
@JsonSerialize(include = NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE)
public class EditorSettings {

    public String typeLabel = "Unknown Type";
    public NodeColor nodeColor = NodeColor.DEFAULT;
    public double positionX;
    public double positionY;
    public String[] components;

    public EditorSettings() {
    }

    public EditorSettings(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public EditorSettings(String typeLabel, NodeColor nodeColor) {
        this.typeLabel = typeLabel;
        this.nodeColor = nodeColor;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public NodeColor getNodeColor() {
        return nodeColor;
    }

    public void setNodeColor(NodeColor nodeColor) {
        this.nodeColor = nodeColor;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public String[] getComponents() {
        return components;
    }

    public void setComponents(String[] components) {
        this.components = components;
    }
}
