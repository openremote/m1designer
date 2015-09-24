package org.openremote.beta.client.shell.messagelog;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
import org.openremote.beta.client.event.FlowDeletedEvent;
import org.openremote.beta.client.event.FlowEditEvent;
import org.openremote.beta.client.event.FlowModifiedEvent;
import org.openremote.beta.shared.event.Message;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageLogPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogPresenter.class);

    public static final int MAX_LOG = 1000;

    public Flow flow;
    public String messageLogTitle;
    public MessageLogDetail[] log = new MessageLogDetail[0];
    public boolean watchAllFlows = true;

    public MessageLogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

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
