package org.openremote.beta.server.route;

import org.apache.camel.Exchange;

import java.util.Locale;

public final class InputValue {

    // Null == Empty String!
    public static String getInput(Exchange exchange) {
        if (exchange.getIn().getBody() == null)
            return "";
        return exchange.getIn().getBody(String.class);
    }

    // Null == Empty String!
    public static void setInput(Exchange exchange, Object body) {
        if (body == null)
            exchange.getIn().setBody("");
        else
            exchange.getIn().setBody(body.toString());
    }

    public static Boolean getBooleanInput(Exchange exchange) {
        String input = getInput(exchange);
        if (input.toLowerCase(Locale.ROOT).equals("true")) {
            return true;
        } else if (input.toLowerCase(Locale.ROOT).equals("false")) {
            return false;
        } else {
            try {
                int inputInt = Integer.valueOf(input);
                if (inputInt == 0) {
                    return false;
                } else if (inputInt == 1) {
                    return true;
                }
            } catch (Exception ex) {
                // Not a number
            }
        }
        return null;
    }
}
