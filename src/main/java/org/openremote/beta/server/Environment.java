package org.openremote.beta.server;

public enum Environment {

    DEV_MODE("true"),
    WEBSERVER_ADDRESS("0.0.0.0"),
    WEBSERVER_PORT("8080"),
    WEBSERVER_DOCUMENT_ROOT("src/main/webapp"),
    WEBSERVER_DEFAULT_CACHE_CONTROL("max-age=300, must-revalidate"),
    WEBSERVER_ALLOW_ORIGIN("*");

    protected String defaultValue;

    Environment(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static String get(Environment environment) {
        String value = System.getenv(environment.name());
        return value != null ? value : environment.defaultValue;
    }
}
