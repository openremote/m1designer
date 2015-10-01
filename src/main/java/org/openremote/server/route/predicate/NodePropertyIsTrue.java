package org.openremote.server.route.predicate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.openremote.shared.flow.Node;


public class NodePropertyIsTrue extends NodePropertiesPredicate {

    final protected String propertyPath;

    public NodePropertyIsTrue(Node node, ObjectNode nodeProperties, String propertyPath) {
        super(node, nodeProperties);
        this.propertyPath = propertyPath;
    }

    @Override
    public boolean matches(Exchange exchange) {
        return getNodeProperties().has(propertyPath)
            && getNodeProperties().get(propertyPath).asBoolean();
    }
}
