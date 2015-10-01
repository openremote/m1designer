package org.openremote.shared.event;

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
