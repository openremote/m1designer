package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;
import org.openremote.beta.shared.flow.Node;

public class PropertyIsSet extends NodePredicate {

    final protected String property;

    public PropertyIsSet(Node node, String property) {
        super(node);
        this.property = property;
    }

    @Override
    public boolean matches(Exchange exchange) {
        return getPropertyValue(property) != null;
    }
}