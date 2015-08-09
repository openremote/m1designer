package org.openremote.beta.shared.event;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;

@JsExport
@JsType
public enum FlowDeploymentPhase {

    NOT_FOUND,
    INITIALIZING_NODES,
    ADDING_ROUTES,
    STARTING,
    DEPLOYED,
    STOPPING,
    REMOVING_ROUTES,
    STOPPED;
}
