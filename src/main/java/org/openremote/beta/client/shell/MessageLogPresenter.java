package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
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

    public boolean active;
    public Flow flow;
    public boolean watchAllFlows;

    public MessageLogPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(MessageLogOpenEvent.class, event -> {
            active = true;
        });

        addEventListener(MessageLogCloseEvent.class, event -> {
            active = false;
        });

        addEventListener(FlowEditEvent.class, true, false, event -> {
            this.flow = event.getFlow();
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
                dispatchEvent(new MessageLogUpdateEvent(detail));
            }
        });
    }
}
