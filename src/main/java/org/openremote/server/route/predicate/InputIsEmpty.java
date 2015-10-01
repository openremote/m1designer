package org.openremote.server.route.predicate;

import org.apache.camel.Exchange;

import static org.openremote.server.route.InputValue.getInput;

public class InputIsEmpty extends InputPredicate {

    @Override
    public boolean matches(Exchange exchange) {
        return getInput(exchange).length() == 0;
    }
}
