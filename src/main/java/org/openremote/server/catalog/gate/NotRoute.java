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

package org.openremote.server.catalog.gate;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.route.NodeRoute;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.LoggingLevel.DEBUG;

public class NotRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(NotRoute.class);

    public NotRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        routeDefinition
            .choice()
            .id(getProcessorId("NOT"))
            .when(isInputTrue())
            .log(DEBUG, LOG, "Input is true, negating")
            .setBody(constant(0))
            .when(isInputFalse())
            .log(DEBUG, LOG, "Input is false, negating")
            .setBody(constant(1))
            .otherwise()
            .log(DEBUG, LOG, "Input is not boolean, stopping")
            .stop()
            .endChoice();
    }
}
