package org.openremote.beta.server.route.predicate;

import org.apache.camel.Predicate;
import org.openremote.beta.shared.flow.Node;


public abstract class NodePredicate implements Predicate {

    final protected Node node;

    public NodePredicate(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

}
