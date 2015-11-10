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

package org.openremote.server.web.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WebsocketCORSFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketCORSFilter.class);

    public static final String ALLOWED_ORIGIN = "ALLOWED_ORIGIN";

    protected String allowedOrigin;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedOrigin = filterConfig.getInitParameter(ALLOWED_ORIGIN);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (allowedOrigin == null) {
            LOG.debug("No origin restriction, allowing Websocket upgrade request");
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (req.getHeader("Upgrade") != null) {
            String origin = req.getHeader("Origin");
            if (origin != null && origin.equals(allowedOrigin)) {
                LOG.debug("Received origin is allowed origin, allowing Websocket upgrade request: " + origin);
                chain.doFilter(request, response);
                return;
            }
        }

        LOG.info("Illegal origin, dropping Websocket upgrade request");
        resp.sendError(400, "Origin is not allowed");
    }

    @Override
    public void destroy() {
    }

}