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

package org.openremote.server.testdata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.server.catalog.filter.FilterNodeDescriptor;
import org.openremote.server.catalog.function.FunctionNodeDescriptor;
import org.openremote.server.catalog.widget.PushButtonNodeDescriptor;
import org.openremote.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.server.route.ConsumerRoute;
import org.openremote.server.route.ProducerRoute;
import org.openremote.server.route.SubflowRoute;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.flow.Flow;
import org.openremote.shared.flow.Node;
import org.openremote.shared.flow.Slot;
import org.openremote.shared.flow.Wire;

import java.io.IOException;
import java.util.List;

import static org.openremote.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.server.util.JsonUtil.JSON;
import static org.openremote.shared.flow.Node.TYPE_SUBFLOW;
import static org.openremote.shared.flow.Slot.TYPE_SINK;
import static org.openremote.shared.flow.Slot.TYPE_SOURCE;

public class SampleThermostatControl {

    /* ###################################################################################### */

    public static Slot TEMPERATURE_CONSUMER_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Slot TEMPERATURE_CONSUMER_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK, false);
    public static Node TEMPERATURE_CONSUMER = new Node("Temperature", generateGlobalUniqueId(), Node.TYPE_CONSUMER);

    static {
        new ConsumerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(TEMPERATURE_CONSUMER_SINK);
                slots.add(TEMPERATURE_CONSUMER_SOURCE);
            }
        }.initialize(TEMPERATURE_CONSUMER);
        TEMPERATURE_CONSUMER.getEditorSettings().setPositionX((double) 20);
        TEMPERATURE_CONSUMER.getEditorSettings().setPositionY((double) 120);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_CONSUMER_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Slot SETPOINT_CONSUMER_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK, false);
    public static Node SETPOINT_CONSUMER = new Node("Setpoint", generateGlobalUniqueId(), Node.TYPE_CONSUMER);

    static {
        new ConsumerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_CONSUMER_SINK);
                slots.add(SETPOINT_CONSUMER_SOURCE);
            }
        }.initialize(SETPOINT_CONSUMER);
        SETPOINT_CONSUMER.getEditorSettings().setPositionX((double) 20);
        SETPOINT_CONSUMER.getEditorSettings().setPositionY((double) 300);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK, SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getLabel());
    public static Slot TEMPERATURE_PROCESSOR_FLOW_CELCIUS_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.CELCIUS_PRODUCER_SOURCE, SampleTemperatureProcessor.CELCIUS_PRODUCER.getLabel());
    public static Slot TEMPERATURE_PROCESSOR_FLOW_LABEL_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE, SampleTemperatureProcessor.LABEL_PRODUCER.getLabel());
    public static Node TEMPERATURE_PROCESSOR_FLOW = new Node(SampleTemperatureProcessor.FLOW.getLabel(), generateGlobalUniqueId(), TYPE_SUBFLOW, SampleTemperatureProcessor.FLOW.getId());

    static {
        new SubflowRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK);
                slots.add(TEMPERATURE_PROCESSOR_FLOW_CELCIUS_SOURCE);
                slots.add(TEMPERATURE_PROCESSOR_FLOW_LABEL_SOURCE);
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }
        }.initialize(TEMPERATURE_PROCESSOR_FLOW);
        TEMPERATURE_PROCESSOR_FLOW.getEditorSettings().setPositionX((double) 300);
        TEMPERATURE_PROCESSOR_FLOW.getEditorSettings().setPositionY((double) 20);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK, SampleTemperatureProcessor.FAHRENHEIT_CONSUMER.getLabel());
    public static Slot SETPOINT_PROCESSOR_FLOW_CELCIUS_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.CELCIUS_PRODUCER_SOURCE, SampleTemperatureProcessor.CELCIUS_PRODUCER.getLabel());
    public static Slot SETPOINT_PROCESSOR_FLOW_LABEL_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE, SampleTemperatureProcessor.LABEL_PRODUCER.getLabel());
    public static Node SETPOINT_PROCESSOR_FLOW = new Node(SampleTemperatureProcessor.FLOW.getLabel(), generateGlobalUniqueId(), TYPE_SUBFLOW, SampleTemperatureProcessor.FLOW.getId());

    static {
        new SubflowRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK);
                slots.add(SETPOINT_PROCESSOR_FLOW_CELCIUS_SOURCE);
                slots.add(SETPOINT_PROCESSOR_FLOW_LABEL_SOURCE);
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }
        }.initialize(SETPOINT_PROCESSOR_FLOW);
        SETPOINT_PROCESSOR_FLOW.getEditorSettings().setPositionX((double) 300);
        SETPOINT_PROCESSOR_FLOW.getEditorSettings().setPositionY((double) 300);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_LABEL_SINK = new Slot("Text", generateGlobalUniqueId(), TYPE_SINK, "text");
    public static Node TEMPERATURE_LABEL = new Node("Temperature Label", generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE);

    static {
        new TextLabelNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(TEMPERATURE_LABEL_SINK);
                slots.add(new Slot("Text", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, "text"));
                slots.add(new Slot("Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "textColor"));
                slots.add(new Slot("Font Size", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "fontSize"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES.deepCopy()
                    .put("textColor", "white")
                    .put("emptyValue", "0 \u00B0C")
                    .put("fontSizePixels", 40)
                    .put("positionX", 80)
                    .put("positionY", 0)
                    .put("positionZ", 0)
                    .put("opacity", 1.0);
            }

        }.initialize(TEMPERATURE_LABEL);
        TEMPERATURE_LABEL.setPostEndpoint("mock:labelTemperature");
        TEMPERATURE_LABEL.getEditorSettings().setPositionX((double) 700);
        TEMPERATURE_LABEL.getEditorSettings().setPositionY((double) 50);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_LABEL_SINK = new Slot("Text", generateGlobalUniqueId(), TYPE_SINK, "text");
    public static Node SETPOINT_LABEL = new Node("Setpoint Label", generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE);

    static {
        new TextLabelNodeDescriptor() {

            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_LABEL_SINK);
                slots.add(new Slot("Text", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SOURCE, "text"));
                slots.add(new Slot("Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "textColor"));
                slots.add(new Slot("Font Size", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "fontSize"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES.deepCopy()
                    .put("textColor", "#ccc")
                    .put("emptyValue", "0 \u00B0C")
                    .put("fontSizePixels", 30)
                    .put("positionX", 90)
                    .put("positionY", 55)
                    .put("positionZ", 0)
                    .put("opacity", 1.0);
            }
        }.initialize(SETPOINT_LABEL);
        SETPOINT_LABEL.setPostEndpoint("mock:labelSetpoint");
        SETPOINT_LABEL.getEditorSettings().setPositionX((double) 700);
        SETPOINT_LABEL.getEditorSettings().setPositionY((double) 370);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PLUS_BUTTON_SOURCE = new Slot("Click", generateGlobalUniqueId(), TYPE_SOURCE, "click");
    public static Node SETPOINT_PLUS_BUTTON = new Node("Increase Temperature Button", generateGlobalUniqueId(), PushButtonNodeDescriptor.TYPE);

    static {
        new PushButtonNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_PLUS_BUTTON_SOURCE);
                slots.add(new Slot("Text", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "text"));
                slots.add(new Slot("Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "backgroundColor"));
                slots.add(new Slot("Text Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "color"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return PushButtonNodeDescriptor.PUSH_BUTTON_INITIAL_PROPERTIES.deepCopy()
                    .put("text", "")
                    .put("fontSizePixels", 30)
                    .put("color", "#c1d72f")
                    .put("backgroundColor", "transparent")
                    .put("icon", "add")
                    .put("raised", false)
                    .put("positionX", 170)
                    .put("positionY", 40);
            }
        }.initialize(SETPOINT_PLUS_BUTTON);
        SETPOINT_PLUS_BUTTON.getEditorSettings().setPositionX((double) 20);
        SETPOINT_PLUS_BUTTON.getEditorSettings().setPositionY((double) 500);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_MINUS_BUTTON_SOURCE = new Slot("Click", generateGlobalUniqueId(), TYPE_SOURCE, "click");
    public static Node SETPOINT_MINUS_BUTTON = new Node("Decrease Temperature Button", generateGlobalUniqueId(), PushButtonNodeDescriptor.TYPE);

    static {
        new PushButtonNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_MINUS_BUTTON_SOURCE);
                slots.add(new Slot("Text", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "text"));
                slots.add(new Slot("Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "backgroundColor"));
                slots.add(new Slot("Text Color", IdentifierUtil.generateGlobalUniqueId(), Slot.TYPE_SINK, "color"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SINK, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SINK, "opacity"));
                slots.add(new Slot("Position X", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionX"));
                slots.add(new Slot("Position Y", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionY"));
                slots.add(new Slot("Position Z", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "positionZ"));
                slots.add(new Slot("Opacity", generateGlobalUniqueId(), Slot.TYPE_SOURCE, "opacity"));
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return PushButtonNodeDescriptor.PUSH_BUTTON_INITIAL_PROPERTIES.deepCopy()
                    .put("text", "")
                    .put("fontSizePixels", 30)
                    .put("color", "#c1d72f")
                    .put("backgroundColor", "transparent")
                    .put("icon", "remove")
                    .put("raised", false)
                    .put("positionX", 0)
                    .put("positionY", 40);
            }
        }.initialize(SETPOINT_MINUS_BUTTON);
        SETPOINT_MINUS_BUTTON.getEditorSettings().setPositionX((double) 20);
        SETPOINT_MINUS_BUTTON.getEditorSettings().setPositionY((double) 820);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PLUS_FILTER_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_PLUS_FILTER_TRIGGER_SINK = new Slot("Trigger", generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_PLUS_FILTER_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Node SETPOINT_PLUS_FILTER = new Node("Forward on trigger", generateGlobalUniqueId(), FilterNodeDescriptor.TYPE);

    static {
        new FilterNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_PLUS_FILTER_SINK);
                slots.add(SETPOINT_PLUS_FILTER_TRIGGER_SINK);
                slots.add(SETPOINT_PLUS_FILTER_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return FilterNodeDescriptor.FILTER_INITIAL_PROPERTIES.deepCopy()
                    .put("waitForTrigger", true);
            }
        }.initialize(SETPOINT_PLUS_FILTER);
        SETPOINT_PLUS_FILTER.getEditorSettings().setPositionX((double) 450);
        SETPOINT_PLUS_FILTER.getEditorSettings().setPositionY((double) 620);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_MINUS_FILTER_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_MINUS_FILTER_TRIGGER_SINK = new Slot("Trigger", generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_MINUS_FILTER_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Node SETPOINT_MINUS_FILTER = new Node("Forward on trigger", generateGlobalUniqueId(), FilterNodeDescriptor.TYPE);

    static {
        new FilterNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_MINUS_FILTER_SINK);
                slots.add(SETPOINT_MINUS_FILTER_TRIGGER_SINK);
                slots.add(SETPOINT_MINUS_FILTER_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return FilterNodeDescriptor.FILTER_INITIAL_PROPERTIES.deepCopy()
                    .put("waitForTrigger", true);
            }
        }.initialize(SETPOINT_MINUS_FILTER);
        SETPOINT_MINUS_FILTER.getEditorSettings().setPositionX((double) 450);
        SETPOINT_MINUS_FILTER.getEditorSettings().setPositionY((double) 790);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_INCREMENT_FUNCTION_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_INCREMENT_FUNCTION_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Node SETPOINT_INCREMENT_FUNCTION = new Node("Increment by 1", generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE);

    static {
        new FunctionNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_INCREMENT_FUNCTION_SINK);
                slots.add(SETPOINT_INCREMENT_FUNCTION_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return FUNCTION_INITIAL_PROPERTIES.deepCopy()
                    .put("javascript", "result = input ? (parseInt(input) + 1).toFixed(0) : null");
            }
        }.initialize(SETPOINT_INCREMENT_FUNCTION);
        SETPOINT_INCREMENT_FUNCTION.getEditorSettings().setPositionX((double) 730);
        SETPOINT_INCREMENT_FUNCTION.getEditorSettings().setPositionY((double) 700);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_DECREMENT_FUNCTION_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_DECREMENT_FUNCTION_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE);
    public static Node SETPOINT_DECREMENT_FUNCTION = new Node("Decrement by 1", generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE);

    static {
        new FunctionNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_DECREMENT_FUNCTION_SINK);
                slots.add(SETPOINT_DECREMENT_FUNCTION_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return FUNCTION_INITIAL_PROPERTIES.deepCopy()
                    .put("javascript", "result = input ? (parseInt(input) - 1).toFixed(0) : null");
            }
        }.initialize(SETPOINT_DECREMENT_FUNCTION);
        SETPOINT_DECREMENT_FUNCTION.getEditorSettings().setPositionX((double) 730);
        SETPOINT_DECREMENT_FUNCTION.getEditorSettings().setPositionY((double) 850);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PRODUCER_SINK = new Slot(generateGlobalUniqueId(), TYPE_SINK);
    public static Slot SETPOINT_PRODUCER_SOURCE = new Slot(generateGlobalUniqueId(), TYPE_SOURCE, false);
    public static Node SETPOINT_PRODUCER = new Node("Setpoint", generateGlobalUniqueId(), Node.TYPE_PRODUCER);

    static {
        new ProducerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(SETPOINT_PRODUCER_SOURCE);
                slots.add(SETPOINT_PRODUCER_SINK);
            }
        }.initialize(SETPOINT_PRODUCER);
        SETPOINT_PRODUCER.setPostEndpoint("mock:producerSetpoint");
        SETPOINT_PRODUCER.getEditorSettings().setPositionX((double) 930);
        SETPOINT_PRODUCER.getEditorSettings().setPositionY((double) 775);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        TEMPERATURE_CONSUMER,
        SETPOINT_CONSUMER,
        TEMPERATURE_LABEL,
        SETPOINT_LABEL,
        SETPOINT_PLUS_BUTTON,
        SETPOINT_MINUS_BUTTON,
        SETPOINT_PLUS_FILTER,
        SETPOINT_MINUS_FILTER,
        SETPOINT_INCREMENT_FUNCTION,
        SETPOINT_DECREMENT_FUNCTION,
        SETPOINT_PRODUCER,
        TEMPERATURE_PROCESSOR_FLOW,
        SETPOINT_PROCESSOR_FLOW
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "Thermostat Control",
        generateGlobalUniqueId(),
        FLOW_NODES,
        new Wire[]{
            new Wire(TEMPERATURE_CONSUMER_SOURCE, TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK),
            new Wire(TEMPERATURE_PROCESSOR_FLOW_LABEL_SOURCE, TEMPERATURE_LABEL_SINK),
            new Wire(SETPOINT_CONSUMER_SOURCE, SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK),
            new Wire(SETPOINT_PROCESSOR_FLOW_LABEL_SOURCE, SETPOINT_LABEL_SINK),
            new Wire(SETPOINT_CONSUMER_SOURCE, SETPOINT_PLUS_FILTER_SINK),
            new Wire(SETPOINT_CONSUMER_SOURCE, SETPOINT_MINUS_FILTER_SINK),
            new Wire(SETPOINT_PLUS_BUTTON_SOURCE, SETPOINT_PLUS_FILTER_TRIGGER_SINK),
            new Wire(SETPOINT_MINUS_BUTTON_SOURCE, SETPOINT_MINUS_FILTER_TRIGGER_SINK),
            new Wire(SETPOINT_PLUS_FILTER_SOURCE, SETPOINT_INCREMENT_FUNCTION_SINK),
            new Wire(SETPOINT_MINUS_FILTER_SOURCE, SETPOINT_DECREMENT_FUNCTION_SINK),
            new Wire(SETPOINT_INCREMENT_FUNCTION_SOURCE, SETPOINT_PRODUCER_SINK),
            new Wire(SETPOINT_DECREMENT_FUNCTION_SOURCE, SETPOINT_PRODUCER_SINK)
        }
    );

    public static Flow getCopy() {
        try {
            return JSON.readValue(JSON.writeValueAsString(FLOW), Flow.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
