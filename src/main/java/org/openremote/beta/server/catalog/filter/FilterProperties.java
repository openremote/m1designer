package org.openremote.beta.server.catalog.filter;

public class FilterProperties {

    protected boolean waitForTrigger;

    public FilterProperties() {
    }

    public FilterProperties(boolean waitForTrigger) {
        this.waitForTrigger = waitForTrigger;
    }

    public boolean isWaitForTrigger() {
        return waitForTrigger;
    }

    public void setWaitForTrigger(boolean waitForTrigger) {
        this.waitForTrigger = waitForTrigger;
    }
}
