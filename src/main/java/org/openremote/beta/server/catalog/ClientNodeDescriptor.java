package org.openremote.beta.server.catalog;

import org.openremote.beta.shared.flow.NodeColor;

public abstract class ClientNodeDescriptor extends NodeDescriptor {

    @Override
    public NodeColor getColor() {
        return NodeColor.CLIENT;
    }
}
