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

package org.openremote.client.shared;

import elemental.client.Browser;
import elemental.events.CloseEvent;
import elemental.html.WebSocket;
import jsinterop.annotations.JsType;
import org.openremote.client.event.*;
import org.openremote.client.event.SessionClosedErrorEvent.Error;
import org.openremote.shared.event.client.ShowFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.shared.Constants.WEBSOCKET_SERVICE_CONTEXT_PATH;

@JsType
public abstract class SessionPresenter<V extends View> extends RequestPresenter<V> {

    private static final Logger LOG = LoggerFactory.getLogger(SessionPresenter.class);

    public static final int MAX_ATTEMPTS = 12;
    public static final int DELAY_MILLIS = 5000;

    final protected String serviceUrl;
    protected WebSocket webSocket;
    protected int failureCount;

    public SessionPresenter(V view, String serviceUrl) {
        super(view);
        this.serviceUrl = serviceUrl;

        addListener(SessionConnectEvent.class, event -> {

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
                    dispatch(new SessionOpenedEvent());
                }
            });
            webSocket.setOnclose(evt -> {
                CloseEvent closeEvent = (CloseEvent) evt;
                if (closeEvent.isWasClean() && closeEvent.getCode() == 1000) {
                    LOG.debug("WebSocket closed: " + webSocket.getUrl());
                    dispatch(new SessionClosedCleanEvent());
                } else {
                    LOG.debug("WebSocket '" + webSocket.getUrl() + "' closed with error: " + closeEvent.getCode());
                    dispatch(new SessionClosedErrorEvent(
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

        addListener(SessionCloseEvent.class, event -> {
            if (webSocket != null) {
                webSocket.close(1000, "SessionCloseEvent");
            }
        });

        addListener(SessionOpenedEvent.class, event -> {
            LOG.debug("Session opened successfully, resetting failure count...");
            failureCount = 0;
        });

        addListener(SessionClosedErrorEvent.class, event -> {
            dispatch(new ShowFailureEvent("Dropped server connection, will try a few more times to reach: " + serviceUrl, 3000));
            LOG.debug("Session closed with error, incrementing failure count: " + failureCount);
            failureCount++;
            if (failureCount < MAX_ATTEMPTS) {
                LOG.debug("Session reconnection attempt '" + serviceUrl + "' with delay milliseconds: " + DELAY_MILLIS);
                dispatch(new SessionConnectEvent(), DELAY_MILLIS);
            } else {
                String failureMessage = "Giving up connecting to service after " + failureCount + " failures: " + serviceUrl;
                dispatch(new ShowFailureEvent(failureMessage, ShowFailureEvent.DURABLE));
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

    protected static String getWebSocketUrl(String... pathElement) {
        StringBuilder sb = new StringBuilder();
        sb.append("ws://").append(hostname()).append(":").append(port()).append(WEBSOCKET_SERVICE_CONTEXT_PATH);
        if (pathElement != null) {
            for (String pe : pathElement) {
                sb.append("/").append(pe);
            }
        }
        return sb.toString();
    }

    protected abstract void onDataReceived(String data);
}
