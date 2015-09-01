package org.openremote.beta.server.route.predicate;

import org.apache.camel.Exchange;

import static org.openremote.beta.server.route.InputValue.getBooleanInput;

public class InputIsFalse extends InputIsEmpty {

    @Override
    public boolean matches(Exchange exchange) {
        Boolean booleanInput = getBooleanInput(exchange);
        return booleanInput != null && !booleanInput;
    }
}
