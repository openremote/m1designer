package org.openremote.server.catalog;

import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.NodeColor;

public abstract class VirtualNodeDescriptor extends NodeDescriptor {

    @Override
    public NodeColor getColor() {
        return NodeColor.VIRTUAL;
    }

    @Override
    public CatalogCategory getCatalogCategory() {
        return CatalogCategory.WIRING;
    }
}
