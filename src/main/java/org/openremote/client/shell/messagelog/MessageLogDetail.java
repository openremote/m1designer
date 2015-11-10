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

import jsinterop.annotations.JsType;
import org.openremote.shared.event.Message;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.client.shell.floweditor.FlowDesignerConstants.SLOT_SINK_LABEL;
import static org.openremote.client.shell.floweditor.FlowDesignerConstants.SLOT_SOURCE_LABEL;

@JsType
public class MessageLogDetail {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogDetail.class);

    public boolean incoming;
    public String flowLabel;
    public String nodeLabel;
    public String slotLabel;
    public String instanceLabel;
    public String body;

    public MessageLogDetail(boolean incoming, Message event, Flow flow, Node node, Slot slot) {
        this.incoming = incoming;

        this.flowLabel = flow != null ? flow.getLabel() : null;

        if (node != null) {
            this.nodeLabel = node.getLabel() != null
                ? node.getLabel() + " (" + node.getEditorSettings().getTypeLabel() + ")"
                : node.getEditorSettings().getTypeLabel();
        }

        if (node == null || slot == null || !(node.getLabel() != null && node.getLabel().equals(slot.getLabel()))) {
            if (slot != null) {
                if (slot.getLabel() != null) {
                    this.slotLabel = slot.getLabel() + (slot.isOfType(Slot.TYPE_SINK) ? " (Sink)" : " (Source)");
                } else {
                    this.slotLabel = slot.isOfType(Slot.TYPE_SINK) ? SLOT_SINK_LABEL : SLOT_SOURCE_LABEL;
                }
            } else {
                this.slotLabel = event.getSlotId();
            }
        }
        this.instanceLabel = event.getInstanceId();

        if (flow != null && event.getInstanceId() != null) {
            Node instanceNode = flow.findNode(event.getInstanceId());
            if (instanceNode != null && instanceNode.getLabel() != null) {
                this.instanceLabel = instanceNode.getLabel();
            }
        }

        this.body = event.getBody() != null && event.getBody().length() > 0 ? event.getBody() : "(EMPTY MESSAGE)";
    }

}
