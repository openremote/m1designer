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
    }

    public SensorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        // TODO Quick and dirty hack that doesn't work, discovery must be restart-able or the ZWConsumer doesn't register its state change listener
        if (getNodeProperties() == null || !getNodeProperties().has("consumerEndpoint"))
            return;
        String consumerEndpoint = getNodeProperties().get("consumerEndpoint").asText();
        if (consumerEndpoint == null)
            return;

        from(consumerEndpoint)
            .routePolicy(new RoutePolicySupport() {
                @Override
                protected void doStart() throws Exception {
                    super.doStart();
                    if (getNodeProperties().has("discoveryEndpoint")) {
                        String discoveryEndpoint = getNodeProperties().get("discoveryEndpoint").asText();
                        LOG.info("### STARTING SENSOR ROUTE'S DISCOVERY: " + discoveryEndpoint);
                        getContext().createProducerTemplate().sendBody(discoveryEndpoint, null);
                    }
                }
            })
            .process(exchange -> {
                String currentStatus = exchange.getIn().getBody(String.class);
                LOG.info("######################## RECEIVED SENSOR STATUS: " + currentStatus);

                Slot[] sourceSlots = getNode().findSlots(Slot.TYPE_SOURCE);
                LOG.debug("Using wires of destination source slots: " + Arrays.toString(sourceSlots));
                for (Slot sourceSlot : sourceSlots) {
                    LOG.debug("Finding wires attached to: " + sourceSlot);
                    Wire[] sourceWires = flow.findWiresForSource(sourceSlot.getId());
                    LOG.debug("Found wires, sending exchange copy to each: " + Arrays.toString(sourceWires));
                    for (Wire sourceWire : sourceWires) {
                        sendExchangeCopy(sourceWire.getSinkId(), exchange, false);
                    }
                }
            });
    }
}
