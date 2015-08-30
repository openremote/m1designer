package org.openremote.beta.shared.flow;

import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.shared.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class FlowDependency extends FlowObject {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDependency.class);

    public Flow flow; // Optional, only in fully materialized dependency tree
    public int level;
    public boolean wired; // Super-dependencies might be only users are have actual wires (hard dependency)

    protected FlowDependency() {
    }

    public FlowDependency(String label, Identifier identifier) {
        super(label, identifier);
    }

    public FlowDependency(String label, Identifier identifier, int level, boolean wired) {
        this(label, identifier, null, level);
        this.wired = wired;
    }

    public FlowDependency(String label, Identifier identifier, Flow flow, int level) {
        super(label, identifier);
        this.flow = flow;
        this.level = level;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isWired() {
        return wired;
    }

    public void setWired(boolean wired) {
        this.wired = wired;
    }
}
