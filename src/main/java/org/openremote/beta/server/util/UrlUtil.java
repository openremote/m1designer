package org.openremote.beta.server.util;

import gumi.builders.UrlBuilder;
import org.apache.camel.Exchange;

import static org.apache.camel.Exchange.HTTP_URL;

public class UrlUtil {

    public static UrlBuilder url(Exchange exchange, String contextPath, String... pathSegments) {
        return url(exchange.getIn().getHeader(HTTP_URL, String.class), contextPath, pathSegments);
    }

    public static UrlBuilder url(String baseUrl, String contextPath, String... pathSegments) {
        return UrlBuilder.fromString(baseUrl)
            .withPath(getPath(contextPath, pathSegments));
    }

    public static UrlBuilder url(String scheme, String host, String port, String contextPath, String... pathSegments) {
        return UrlBuilder.empty()
            .withScheme(scheme)
            .withHost(host)
            .withPort(port != null ? Integer.valueOf(port) : null)
            .withPath(getPath(contextPath, pathSegments));
    }

    protected static String getPath(String contextPath, String... pathSegments) {
        StringBuilder path = new StringBuilder();
        path.append(contextPath);
        if (pathSegments != null) {
            for (String pathSegment : pathSegments) {
                path
                    .append(pathSegment.startsWith("/") ? "" : "/")
                    .append(pathSegment);
            }
        }
        return path.toString();
    }
}
