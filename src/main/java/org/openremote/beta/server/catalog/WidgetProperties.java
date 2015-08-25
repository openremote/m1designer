package org.openremote.beta.server.catalog;

public class WidgetProperties {

    protected String component;
    protected int positionX;
    protected int positionY;

    public WidgetProperties() {
    }

    public WidgetProperties(String component, int positionX, int positionY) {
        this.component = component;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
}
