package org.openremote.beta.server.catalog.change;

public class ChangeProperties {

    protected String prepend;
    protected String append;

    public ChangeProperties() {
    }

    public ChangeProperties(String prepend, String append) {
        this.prepend = prepend;
        this.append = append;
    }

    public String getPrepend() {
        return prepend;
    }

    public void setPrepend(String prepend) {
        this.prepend = prepend;
    }

    public String getAppend() {
        return append;
    }

    public void setAppend(String append) {
        this.append = append;
    }
}
