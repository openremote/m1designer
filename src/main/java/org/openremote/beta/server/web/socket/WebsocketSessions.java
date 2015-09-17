package org.openremote.beta.server.web.socket;

import java.util.Collection;

import org.apache.camel.Service;

import javax.websocket.Session;

public interface WebsocketSessions extends Service {

    void add(Session websocketSession);

    void remove(Session websocketSession);

    Session get(String sessionId);

    Collection<Session> getAll();
}
