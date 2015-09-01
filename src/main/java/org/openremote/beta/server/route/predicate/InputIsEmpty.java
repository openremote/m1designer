package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;

import static org.openremote.beta.server.route.InputValue.getInput;

public class InputIsEmpty extends InputPredicate {

    @Override
    public boolean matches(Exchange exchange) {
        return getInput(exchange).length() == 0;
    }
}
