package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.FlowEditEvent;
import org.openremote.beta.client.editor.flow.FlowModifiedEvent;
import org.openremote.beta.client.editor.flow.FlowDeletedEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.event.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.event.MessageSendEvent;
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
    public String messageLogTitle = "Message Log";
    public MessageLogDetail[] log = new MessageLogDetail[0];
    public boolean watchAllFlows;

    public MessageLogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(MessageLogOpenEvent.class, event -> {
            watchAllFlows = true;
            notifyPath("watchAllFlows", watchAllFlows);
            setMessageLogTitle();
        });

        addEventListener(FlowEditEvent.class, false, event -> {
            flow = event.getFlow();
            notifyPath("flow");
            watchAllFlows = false;
            notifyPath("watchAllFlows", watchAllFlows);
            setMessageLogTitle();
        });

        addEventListener(FlowModifiedEvent.class, event -> {
            if (this.flow != null && this.flow.getId().equals(event.getFlow().getId())) {
                this.flow = event.getFlow();
                notifyPath("flow");
                setMessageLogTitle();
            }
        });

        addEventListener(FlowDeletedEvent.class, event -> {
            if (this.flow != null && this.flow.getId().equals(event.getFlow().getId())) {
                this.flow = null;
                notifyPath("flow");
                setMessageLogTitle();
            }
        });

        addEventListener(MessageReceivedEvent.class, event -> {
            updateMessageLog(true, event.getMessage());
        });

        addEventListener(MessageSendEvent.class, event -> {
            updateMessageLog(false, event.getMessage());
        });
    }

    public void setMessageLogTitle() {
        messageLogTitle = "Message Log (";
        if (flow != null && !flow.isLabelEmpty())
            messageLogTitle += flow.getLabel() + ", ";
        messageLogTitle += log.length + ")";
        notifyPath("messageLogTitle", messageLogTitle);
    }

    public void clearMessageLog() {
        this.log = new MessageLogDetail[0];
        notifyPath("log", log);
        setMessageLogTitle();
    }

    protected void updateMessageLog(boolean incoming, Message event) {
        if (log.length >= MAX_LOG) {
            clearMessageLog();
        }

        Node node = null;
        Slot slot = null;
        if (flow != null) {
            node = flow.findOwnerNode(event.getSlotId());
            if (node != null)
                slot = node.findSlot(event.getSlotId());
        }

        if (watchAllFlows || node != null) {
            MessageLogDetail detail = new MessageLogDetail(
                incoming, event, node != null ? flow : null, node, slot
            );
            pushArray("log", detail);
        }
        setMessageLogTitle();

    }
}
