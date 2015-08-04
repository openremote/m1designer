package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;
import org.openremote.beta.shared.flow.Node;

import java.util.Locale;

public class PropertyIsEquals extends NodePredicate {

    final protected String property;
    final protected String value;
    final protected boolean casesSensitive;

    public PropertyIsEquals(Node node, String property, String value) {
        this(node, property, value, true);
    }

    public PropertyIsEquals(Node node, String property, String value, boolean casesSensitive) {
        super(node);
        this.property = property;
        this.value = !casesSensitive ? value.toLowerCase(Locale.ROOT) : value;
        this.casesSensitive = casesSensitive;
    }

    @Override
    public boolean matches(Exchange exchange) {
        if (value == null)
            return false;
        String propertyValue = getPropertyValue(property);
        if (propertyValue == null)
            return false;
        if (!casesSensitive) {
            propertyValue = propertyValue.toLowerCase(Locale.ROOT);
        }
        return value.equals(propertyValue);
    }
}