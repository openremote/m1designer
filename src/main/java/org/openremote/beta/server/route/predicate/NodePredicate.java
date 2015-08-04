package org.openremote.beta.server.route.predicate;

import org.apache.camel.Predicate;
import org.openremote.beta.shared.flow.Node;

import static org.openremote.beta.shared.util.Util.getMap;
import static org.openremote.beta.shared.util.Util.getString;

public abstract class NodePredicate implements Predicate {

    final protected Node node;

    public NodePredicate(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public String getPropertyValue(String property) {
        if (!getNode().hasProperties())
            return null;
        return getString(getMap(node.getProperties()), property);
    }

}
