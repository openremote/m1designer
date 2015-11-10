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

package org.openremote.server.route.predicate;

import org.apache.camel.Exchange;
import org.openremote.server.route.RouteConstants;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;

public class SinkSlotPosition extends NodePredicate {

    final protected int position;

    public SinkSlotPosition(Node node, int position) {
        super(node);
        this.position = position;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Slot sink = getNode().findSlotByPosition(position, Slot.TYPE_SINK);
        if (sink == null)
            return false;
        String exchangeSlotId = exchange.getIn().getHeader(RouteConstants.SLOT_ID, String.class);
        return exchangeSlotId != null && exchangeSlotId.equals(sink.getId());
    }
}
