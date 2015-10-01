package org.openremote.client.shell.flowcontrol;

import com.google.gwt.core.client.js.JsType;
import org.openremote.shared.event.FlowDeploymentFailureEvent;
import org.openremote.shared.event.FlowDeploymentPhase;

import static org.openremote.shared.event.FlowDeploymentPhase.*;

@JsType
public class FlowStatusDetail {

    public static final String MARK_DEPLOYED = "deployed";
    public static final String MARK_DEPLOYING = "deploying";
    public static final String MARK_PROBLEM = "problem";

    public String mark;
    public boolean canStart;
    public boolean canStop;
    public String text;

    public FlowStatusDetail(String text) {
        this(null, false, false, text);
    }

    public FlowStatusDetail(String mark, boolean canStart, boolean canStop, String text) {
        this.mark = mark;
        this.canStart = canStart;
        this.canStop = canStop;
        this.text = text;
    }

    public FlowStatusDetail(FlowDeploymentPhase phase) {
        switch (phase) {
            case STARTING:
                mark = MARK_DEPLOYING;
                text = STARTING.name();
                canStart = false;
                canStop = false;
                break;
            case DEPLOYED:
                mark = MARK_DEPLOYED;
                text = DEPLOYED.name();
                canStart = false;
                canStop = true;
                break;
            case STOPPING:
                mark = MARK_DEPLOYING;
                text = STOPPING.name();
                canStart = false;
                canStop = false;
                break;
            case STOPPED:
                mark = null;
                text = STOPPED.name();
                canStart = true;
                canStop = false;
                break;
            case NOT_FOUND:
                mark = MARK_PROBLEM;
                text = "FLOW NOT FOUND ON SERVER";
                canStart = false;
                canStop = false;
                break;
            default:
                mark = MARK_PROBLEM;
                text = "DEPLOYMENT PROBLEM WHILE " + phase.name();
                canStart = false;
                canStop = false;
                break;
        }
    }

    public FlowStatusDetail(FlowDeploymentFailureEvent failureEvent) {
        // TODO More details in event
        this(failureEvent.getPhase());
    }

    @Override
    public String toString() {
        return "FlowStatusDetail{" +
            "text='" + text + '\'' +
            '}';
    }
}