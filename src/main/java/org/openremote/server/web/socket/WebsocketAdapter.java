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

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

public class WebsocketAdapter extends Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketAdapter.class);

    final protected WebsocketConsumer consumer;

    public WebsocketAdapter(WebsocketConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        LOG.debug("Websocket session open: {}", session.getId());
        consumer.getEndpoint().getWebsocketSessions().add(session);

        session.addMessageHandler(String.class, message -> {
            LOG.debug("Websocket session {} message received: {}", session.getId(), message);
            this.consumer.sendMessage(session.getId(), message);
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        LOG.debug("Websocket session close: {} {}", session.getId(), closeReason);
        consumer.getEndpoint().getWebsocketSessions().remove(session);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);
        LOG.info("Websocket session error: " + session.getId(), thr);
        consumer.getEndpoint().getWebsocketSessions().remove(session);
    }
}
