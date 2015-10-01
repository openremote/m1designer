package org.openremote.server.route.predicate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Predicate;
import org.openremote.shared.flow.Node;


public abstract class NodePropertiesPredicate implements Predicate {

    final protected Node node;
    final protected ObjectNode nodeProperties;

    public NodePropertiesPredicate(Node node, ObjectNode nodeProperties) {
        this.node = node;
        this.nodeProperties = nodeProperties;
    }

    public Node getNode() {
        return node;
    }

    public ObjectNode getNodeProperties() {
        return nodeProperties;
    }
}
