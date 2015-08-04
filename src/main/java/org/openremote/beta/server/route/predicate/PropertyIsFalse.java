package org.openremote.beta.server.route.predicate;

import org.openremote.beta.shared.flow.Node;

public class PropertyIsFalse extends PropertyIsEquals {
    public PropertyIsFalse(Node node, String property) {
        super(node, property, "false", false);
    }
}
