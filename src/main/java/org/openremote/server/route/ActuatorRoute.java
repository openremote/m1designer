package org.openremote.server.route;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.RoutePolicySupport;
import org.openremote.server.catalog.NodeDescriptor;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.NodeColor;
import org.openremote.shared.flow.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.openremote.beta.zwave.component.ZWComponent.HEADER_COMMAND;

public class ActuatorRoute extends NodeRoute {

    private static final Logger LOG = LoggerFactory.getLogger(ActuatorRoute.class);

    public static final String NODE_TYPE = "urn:openremote:flow:node:actuator";
    public static final String NODE_TYPE_LABEL = "Actuator";

    public static class Descriptor extends NodeDescriptor {

        @Override
        public boolean isInternal() {
            return true;
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
            propertyPaths.add("producerEndpoint");
            propertyPaths.add("zwaveCommand");
        }
    }

    public ActuatorRoute(CamelContext context, Flow flow, Node node) {
        super(context, flow, node);
    }

    @Override
    protected void configureProcessing(ProcessorDefinition routeDefinition) throws Exception {

        // TODO Quick and dirty hack

        LOG.info("### TODO Configure actuator route, with node properties: " + getNodeProperties());
        if (getNodeProperties() == null || !getNodeProperties().has("producerEndpoint"))
            return;

        routeDefinition.process(exchange -> {
            String producerEndpoint = getNodeProperties().get("producerEndpoint").asText();
            LOG.info("### TODO Using actuator producer endpoint: " + producerEndpoint);
            String zwaveCommand = getNodeProperties().get("zwaveCommand").asText();
            LOG.info("### TODO Using zwave command: " + zwaveCommand);
            producerTemplate.sendBodyAndHeader(
                producerEndpoint, exchange.getIn().getBody(String.class), HEADER_COMMAND, zwaveCommand
            );
        });
    }
}
