package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.model.Properties;

public class PropertyIsSet extends NodePredicate {

    final protected String property;

    public PropertyIsSet(Node node, String property) {
        super(node);
        this.property = property;
    }

    @Override
    public boolean matches(Exchange exchange) {
        return Properties.isSet(node.getProperties(), property);
    }
}