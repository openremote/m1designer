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

package org.openremote.client.shell.messagelog;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.openremote.client.event.FlowDeletedEvent;
import org.openremote.client.event.FlowEditEvent;
import org.openremote.client.event.FlowModifiedEvent;
import org.openremote.client.event.ShortcutEvent;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.openremote.shared.event.Message;
import org.openremote.shared.event.client.MessageReceivedEvent;
import org.openremote.shared.event.client.MessageSendEvent;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class MessageLogPresenter extends AbstractPresenter<MessageLogPresenter.MessageLogView> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogPresenter.class);

    @JsType(isNative = true)
    public interface MessageLogView extends View {
        void toggleMessageLog();
    }

    public static final int MAX_LOG = 1000;

    public Flow flow;
    public String messageLogTitle;
    public MessageLogDetail[] log = new MessageLogDetail[0];
    public boolean watchAllFlows = true;

    public MessageLogPresenter(MessageLogView view) {
        super(view);

        addListener(ShortcutEvent.class, event -> {
            if (event.getKey() == 77)
                getView().toggleMessageLog();
        });

        addListener(FlowEditEvent.class, event -> {
            flow = event.getFlow();
            notifyPath("flow");
            watchAllFlows = false;
            notifyPath("watchAllFlows", watchAllFlows);
            setMessageLogTitle();
        });

        addListener(FlowModifiedEvent.class, event -> {
            this.flow = event.getFlow();
            notifyPath("flow");
            setMessageLogTitle();
        });

        addListener(FlowDeletedEvent.class, event -> {
            this.flow = null;
            notifyPathNull("flow");
            setMessageLogTitle();
        });

        addListener(MessageReceivedEvent.class, event-> {
            updateMessageLog(true, event.getMessage());
        });

        addListener(MessageSendEvent.class, event-> {
            updateMessageLog(false, event.getMessage());
        });
    }

    @Override
    public void attached() {
        super.attached();
        setMessageLogTitle();
    }

    public void setMessageLogTitle() {
        messageLogTitle = "Message Log (";
        messageLogTitle += log.length + ")";
        notifyPath("messageLogTitle", messageLogTitle);
    }

    public void clearMessageLog() {
        this.log = new MessageLogDetail[0];
        notifyPath("log", log);
        setMessageLogTitle();
    }

    protected void updateMessageLog(boolean incoming, Message message) {
        if (log.length >= MAX_LOG) {
            clearMessageLog();
        }

        Node node = null;
        Slot slot = null;
        if (flow != null) {
            node = flow.findOwnerNode(message.getSlotId());
            if (node != null)
                slot = node.findSlot(message.getSlotId());
        }

        if (watchAllFlows || node != null) {
            MessageLogDetail detail = new MessageLogDetail(
                incoming, message, node != null ? flow : null, node, slot
            );
            pushArray("log", detail);
        }
        setMessageLogTitle();
    }
}
