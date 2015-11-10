/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.route;

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
