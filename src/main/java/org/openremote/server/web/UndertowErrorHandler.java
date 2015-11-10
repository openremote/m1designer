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

package org.openremote.server.web;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UndertowErrorHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowErrorHandler.class);

    final protected boolean devMode;
    final protected HttpHandler nextHandler;

    public UndertowErrorHandler(boolean devMode, HttpHandler nextHandler) {
        this.devMode = devMode;
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            nextHandler.handleRequest(exchange);
        } catch (Exception ex) {

            LOG.debug(exchange.getRequestMethod() + " " + exchange.getRequestPath(), ex);

            if (exchange.isResponseChannelAvailable()) {
                try {
                    if (devMode) {
                        exchange.getResponseSender().send(renderTrace(ex));
                    } else {
                        exchange.getResponseSender().send(renderGeneric());
                    }
                } catch (Exception ex2) {
                    LOG.error("Couldn't render server error response (in DEVMODE)", ex2);
                }
            }
        }
    }

    protected String renderTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return "500 Server Error\n\n" + sw.toString();
    }

    protected static String renderGeneric() {
        return "Request failed due to server error, please check logs and contact the help desk.";
    }

}
