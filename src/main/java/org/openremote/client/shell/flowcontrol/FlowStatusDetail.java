/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.shell.flowcontrol;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
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

    @JsIgnore
    public FlowStatusDetail(String text) {
        this(null, false, false, text);
    }

    @JsIgnore
    public FlowStatusDetail(String mark, boolean canStart, boolean canStop, String text) {
        this.mark = mark;
        this.canStart = canStart;
        this.canStop = canStop;
        this.text = text;
    }

    @JsIgnore
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

    @JsIgnore
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