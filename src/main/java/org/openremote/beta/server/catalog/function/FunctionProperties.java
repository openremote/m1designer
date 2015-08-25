package org.openremote.beta.server.catalog.function;

public class FunctionProperties {

    protected String javascript;

    public FunctionProperties() {
    }

    public FunctionProperties(String javascript) {
        this.javascript = javascript;
    }

    public String getJavascript() {
        return javascript;
    }

    public void setJavascript(String javascript) {
        this.javascript = javascript;
    }
}
