package org.openremote.beta.client.shared.session;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.events.CloseEvent;
import elemental.html.MetaElement;
import elemental.html.WebSocket;
import org.openremote.beta.client.shared.ShowFailureEvent;
import org.openremote.beta.client.shared.ShowInfoEvent;
import org.openremote.beta.client.shared.request.RequestPresenter;
import org.openremote.beta.client.shared.session.SessionClosedErrorEvent.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
@JsExport
public abstract class SessionPresenter extends RequestPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(SessionPresenter.class);

    public static final int MAX_ATTEMPTS = 12;
    public static final int DELAY_MILLIS = 5000;

    final protected String serviceUrl;
    protected WebSocket webSocket;
    protected int failureCount;
    protected int currentReconnectAttempt = -1;

    public SessionPresenter(com.google.gwt.dom.client.Element view, String serviceUrl) {
        super(view);
        this.serviceUrl = serviceUrl;

        addEventListener(SessionConnectEvent.class, event -> {

            if (webSocket != null) {
                if (webSocket.getReadyState() != WebSocket.CLOSED) {
                    // Close silently
                    LOG.debug(
                        "New connection attempt to '" + serviceUrl + "', closing " +
                            "stale existing connection silently: " + webSocket.getUrl()
                    );
                    webSocket.setOnclose(null);
                    webSocket.close();
                }
                webSocket = null;
            }

            webSocket = Browser.getWindow().newWebSocket(serviceUrl);
            webSocket.setOnopen(evt -> {
                if (webSocket.getReadyState() == WebSocket.OPEN) {
                    LOG.debug("WebSocket open: " + webSocket.getUrl());
                    dispatchEvent(new SessionOpenedEvent());
                }
            });
            webSocket.setOnclose(evt -> {
                CloseEvent closeEvent = (CloseEvent) evt;
                if (closeEvent.isWasClean() && closeEvent.getCode() == 1000) {
                    LOG.debug("WebSocket closed: " + webSocket.getUrl());
                    dispatchEvent(new SessionClosedCleanEvent());
                } else {
                    LOG.debug("WebSocket '" + webSocket.getUrl() + "' closed with error: " + closeEvent.getCode());
                    dispatchEvent(new SessionClosedErrorEvent(
                        new Error(closeEvent.getCode(), closeEvent.getReason())
                    ));
                }
            });
            webSocket.setOnmessage(evt -> {
                elemental.events.MessageEvent messageEvent = (elemental.events.MessageEvent) evt;
                String data = messageEvent.getData().toString();
                LOG.debug("Received data on WebSocket '" + webSocket.getUrl() + "': " + data);
                onDataReceived(data);
            });
        });

        addEventListener(SessionCloseEvent.class, event -> {
            if (webSocket != null) {
                webSocket.close(1000, "SessionCloseEvent");
            }
        });

        addEventListener(SessionOpenedEvent.class, event -> {
            if (currentReconnectAttempt != -1) {
                LOG.debug("Session opened successfully, resetting failure count...");
                dispatchEvent(new ShowInfoEvent("Reconnected successfully to server: " + serviceUrl));
                failureCount = 0;
                currentReconnectAttempt = -1;
            }
        });

        addEventListener(SessionClosedErrorEvent.class, event -> {
            dispatchEvent(new ShowFailureEvent("Dropped server connection, will try a few more times to reach: " + serviceUrl, 3000));
            LOG.debug("Session closed with error, incrementing failure count: " + failureCount);
            failureCount++;
            if (failureCount < MAX_ATTEMPTS) {
                LOG.debug("Session reconnection attempt '" + serviceUrl + "' with delay milliseconds: " + DELAY_MILLIS);
                currentReconnectAttempt = dispatchEvent(
                    new SessionConnectEvent(),
                    DELAY_MILLIS,
                    currentReconnectAttempt
                );
            } else {
                String failureMessage = "Giving up connecting to service after " + failureCount + " failures: " + serviceUrl;
                dispatchEvent(new ShowFailureEvent(failureMessage, ShowFailureEvent.DURABLE));
                LOG.error(failureMessage);
            }
        });

    }

    protected void sendData(String data) {
        if (webSocket != null && webSocket.getReadyState() == WebSocket.OPEN) {
            LOG.debug("Sending data on WebSocket '" + webSocket.getUrl() + "': " + data);
            webSocket.send(data);
        } else {
            LOG.debug("WebSocket not connected, discarding: " + data);
        }
    }

    protected static String getWebSocketHost() {
        MetaElement metaWebSocketHost =
            (MetaElement) Browser.getDocument().querySelector("meta[name=webSocketHost]");
        if (metaWebSocketHost == null)
            return hostname();
        return metaWebSocketHost.getContent();
    }

    protected static String getWebSocketPort() {
        MetaElement metaWebSocketPort =
            (MetaElement) Browser.getDocument().querySelector("meta[name=webSocketPort]");
        if (metaWebSocketPort == null)
            return "9292";
        return metaWebSocketPort.getContent();
    }

    protected static String getWebSocketUrl(String... pathElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("ws://").append(getWebSocketHost()).append(":").append(getWebSocketPort());
        if (pathElement != null) {
            for (String pe : pathElement) {
                sb.append("/").append(pe);
            }
        }
        return sb.toString();
    }

    protected abstract void onDataReceived(String data);
}
