package org.openremote.beta.server.web.socket;

import javax.websocket.Session;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryWebsocketSessions extends ConcurrentHashMap<String, Session> implements WebsocketSessions {

    @Override
    public void add(Session websocketSession) {
        super.put(websocketSession.getId(), websocketSession);
    }

    @Override
    public void remove(Session websocketSession) {
        super.remove(websocketSession.getId());
    }

    @Override
    public Session get(String sessionId) {
        return super.get(sessionId);
    }

    @Override
    public Collection<Session> getAll() {
        return super.values();
    }

    @Override
    public void start() throws Exception {
        // noop
    }

    @Override
    public void stop() throws Exception {
        clear();
    }
}
