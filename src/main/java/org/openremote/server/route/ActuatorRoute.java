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

package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.NodeColor;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ActuatorRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ActuatorRoute.class);

    public static final String NODE_TYPE = "urn:openremote:flow:node:actuator";
    public static final String NODE_TYPE_LABEL = "Actuator";

    public static final String NODE_PROPERTY_ACTUATOR_ENDPOINT = "actuatorEndpoint";

    public static class Descriptor extends NodeDescriptor {

        @Override
        public CatalogCategory getCatalogCategory() {
            return null;
        }

        @Override
        public String getType() {
            return NODE_TYPE;
        }

        @Override
        public String getTypeLabel() {
            return NODE_TYPE_LABEL;
        }

        @Override
        public NodeRoute createRoute(CamelContext context, Flow flow, Node node) {
            return new ActuatorRoute(context, flow, node);
        }

        @Override
        public NodeColor getColor() {
            return NodeColor.SENSOR_ACTUATOR;
        }

        @Override
        public Node initialize(Node node) {
            node = super.initialize(node);
            node.setClientAccess(true);
            return node;
        }

        @Override
        public void addSlots(List<Slot> slots) {
            super.addSlots(slots);
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK));
        }

        @Override
        protected void addPersistentPropertyPaths(List<String> propertyPaths) {
            super.addPersistentPropertyPaths(propertyPaths);
            propertyPaths.add(NODE_PROPERTY_ACTUATOR_ENDPOINT);
        }

        @Override
        public void addEditorComponents(List<String> editorComponents) {
            super.addEditorComponents(editorComponents);
            editorComponents.add("or-node-editor-actuator");
        }
    }

    public ActuatorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {
        String actuatorEndpoint;
        if (getNodeProperties() == null
            || !getNodeProperties().has(NODE_PROPERTY_ACTUATOR_ENDPOINT)
            || (actuatorEndpoint = getNodeProperties().get(NODE_PROPERTY_ACTUATOR_ENDPOINT).asText()).length() == 0) {
            LOG.debug("No processing in actuator node, missing '"+ NODE_PROPERTY_ACTUATOR_ENDPOINT + "' property:" + getNode());
            return;
        }

        // TODO Should we send the headers?
        routeDefinition.process(exchange -> {
            LOG.debug("Sending message to actuator endpoint: " + actuatorEndpoint);
            producerTemplate.sendBody(
                actuatorEndpoint, exchange.getIn().getBody(String.class)
            );
        });
    }
}
