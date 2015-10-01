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