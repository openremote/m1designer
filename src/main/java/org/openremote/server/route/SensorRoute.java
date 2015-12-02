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
import org.apache.camel.support.RoutePolicySupport;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.flow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SensorRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(SensorRoute.class);

    public static final String NODE_TYPE = "urn:openremote:flow:node:sensor";
    public static final String NODE_TYPE_LABEL = "Sensor";

    public static final String NODE_PROPERTY_CONSUMER_ENDPOINT = "consumerEndpoint";
    public static final String NODE_PROPERTY_DISCOVERY_ENDPOINT = "discoveryEndpoint";

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
            return new SensorRoute(context, flow, node);
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
            slots.add(new Slot(IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE));
        }

        @Override
        protected void addPersistentPropertyPaths(List<String> propertyPaths) {
            super.addPersistentPropertyPaths(propertyPaths);
            propertyPaths.add("consumerEndpoint");
            propertyPaths.add("discoveryEndpoint");
        }

        @Override
        public void addEditorComponents(List<String> editorComponents) {
            super.addEditorComponents(editorComponents);
            editorComponents.add("or-node-editor-sensor");
        }
    }

    public SensorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        String consumerEndpoint;
        if (getNodeProperties() == null
            || !getNodeProperties().has(NODE_PROPERTY_CONSUMER_ENDPOINT)
            || (consumerEndpoint = getNodeProperties().get(NODE_PROPERTY_CONSUMER_ENDPOINT).asText()).length() == 0) {
            LOG.debug("No processing in sensor node, missing 'consumerEndpoint' property:" + getNode());
            return;
        }

        from(consumerEndpoint)
            .routePolicy(new RoutePolicySupport() {
                @Override
                protected void doStart() throws Exception {
                    super.doStart();

                    String discoveryEndpoint;
                    if (getNodeProperties() == null
                        || !getNodeProperties().has(NODE_PROPERTY_DISCOVERY_ENDPOINT)
                        || (discoveryEndpoint = getNodeProperties().get(NODE_PROPERTY_DISCOVERY_ENDPOINT).asText()).length() == 0) {
                        return;
                    }
                    LOG.debug("On route start, triggering discovery endpoint '" + discoveryEndpoint + "' of sensor node: " + getNode());
                    getContext().createProducerTemplate().sendBody(discoveryEndpoint, null);
                }
            })
            .process(exchange -> {
                String currentStatus = exchange.getIn().getBody(String.class);
                LOG.debug("Processing received sensor status: " + currentStatus);
                sendExchange(getNode().findSlots(Slot.TYPE_SOURCE), exchange);
            });
    }
}
