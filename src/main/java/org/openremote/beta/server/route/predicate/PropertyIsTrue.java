package org.openremote.beta.server.route.predicate;

import org.openremote.beta.shared.flow.Node;

public class PropertyIsTrue extends PropertyIsEquals {
    public PropertyIsTrue(Node node, String property) {
        super(node, property, "true", false);
    }
}
