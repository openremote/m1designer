package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.crud.FlowDeletedEvent;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.editor.flow.editor.FlowUpdatedEvent;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.shared.event.MessageEvent;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class MessageLogPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogPresenter.class);

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

        addEventListener(FlowUpdatedEvent.class, event -> {
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
            MessageEvent msgEvent = event.getMessageEvent();

            Flow msgFlow = null;
            Node msgNode = null;
            Slot msgSlot = null;
            if (flow != null) {
                msgFlow = flow.findOwnerFlowOfSlot(msgEvent.getSinkSlotId());
                if (msgFlow != null)
                    msgNode = msgFlow.findOwnerNode(msgEvent.getSinkSlotId());
                if (msgNode != null)
                    msgSlot = msgNode.findSlot(msgEvent.getSinkSlotId());
            }

            if (watchAllFlows || msgFlow != null) {
                MessageLogDetail detail = new MessageLogDetail(
                    msgEvent, msgFlow, msgNode, msgSlot
                );
                pushArray("log", detail);
            }
            setMessageLogTitle();
        });
    }

    public void setMessageLogTitle() {
        messageLogTitle = "Message Log (";
        if (flow != null && flow.getLabel() != null && flow.getLabel().length() > 0)
            messageLogTitle += "Flow: " + flow.getLabel() + ", ";
        messageLogTitle += "Received: " + log.length + ")";
        notifyPath("messageLogTitle", messageLogTitle);
    }

    public void clearMessageLog() {
        this.log = new MessageLogDetail[0];
        notifyPath("log", log);
    }
}
