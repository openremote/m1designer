package org.openremote.beta.server.testdata;

import org.openremote.beta.server.catalog.filter.FilterNodeDescriptor;
import org.openremote.beta.server.catalog.function.FunctionNodeDescriptor;
import org.openremote.beta.server.catalog.widget.PushButtonNodeDescriptor;
import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.server.route.ConsumerRoute;
import org.openremote.beta.server.route.ProducerRoute;
import org.openremote.beta.server.route.SubflowRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;
import org.openremote.beta.shared.widget.Widget;

import java.io.IOException;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;
import static org.openremote.beta.shared.flow.Node.*;
import static org.openremote.beta.shared.flow.Slot.TYPE_SINK;
import static org.openremote.beta.shared.flow.Slot.TYPE_SOURCE;

public class SampleThermostatControl {

    /* ###################################################################################### */

    public static Slot TEMPERATURE_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Slot TEMPERATURE_CONSUMER_SINK = new Slot("Temperature", new Identifier(generateGlobalUniqueId(), TYPE_SINK), false);
    public static Node TEMPERATURE_CONSUMER = new Node("Temperature", new Identifier(generateGlobalUniqueId(), ConsumerRoute.NODE_TYPE), TEMPERATURE_CONSUMER_SINK, TEMPERATURE_CONSUMER_SOURCE);

    static {
        new ConsumerRoute.Descriptor().initialize(TEMPERATURE_CONSUMER);
        TEMPERATURE_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_X, 20);
        TEMPERATURE_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_Y, 20);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Slot SETPOINT_CONSUMER_SINK = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), TYPE_SINK), false);
    public static Node SETPOINT_CONSUMER = new Node("Setpoint", new Identifier(generateGlobalUniqueId(), ConsumerRoute.NODE_TYPE), SETPOINT_CONSUMER_SINK, SETPOINT_CONSUMER_SOURCE);

    static {
        new ConsumerRoute.Descriptor().initialize(SETPOINT_CONSUMER);
        SETPOINT_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_X, 20);
        SETPOINT_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_Y, 200);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK);
    public static Slot TEMPERATURE_PROCESSOR_FLOW_CELCIUS_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.CELCIUS_PRODUCER_SOURCE);
    public static Slot TEMPERATURE_PROCESSOR_FLOW_LABEL_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE);
    public static Node TEMPERATURE_PROCESSOR_FLOW = new Node(SampleTemperatureProcessor.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), TYPE_SUBFLOW), TEMPERATURE_PROCESSOR_FLOW_FAHRENHEIT_SINK, TEMPERATURE_PROCESSOR_FLOW_CELCIUS_SOURCE, TEMPERATURE_PROCESSOR_FLOW_LABEL_SOURCE);

    static {
        new SubflowRoute.Descriptor().initialize(TEMPERATURE_PROCESSOR_FLOW);
        TEMPERATURE_PROCESSOR_FLOW.getEditorProperties().put(EDITOR_PROPERTY_X, 300);
        TEMPERATURE_PROCESSOR_FLOW.getEditorProperties().put(EDITOR_PROPERTY_Y, 20);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.FAHRENHEIT_CONSUMER_SINK);
    public static Slot SETPOINT_PROCESSOR_FLOW_CELCIUS_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.CELCIUS_PRODUCER_SOURCE);
    public static Slot SETPOINT_PROCESSOR_FLOW_LABEL_SOURCE = new Slot(generateGlobalUniqueId(), SampleTemperatureProcessor.LABEL_PRODUCER_SOURCE);
    public static Node SETPOINT_PROCESSOR_FLOW = new Node(SampleTemperatureProcessor.FLOW.getLabel(), new Identifier(generateGlobalUniqueId(), TYPE_SUBFLOW), SETPOINT_PROCESSOR_FLOW_FAHRENHEIT_SINK, SETPOINT_PROCESSOR_FLOW_CELCIUS_SOURCE, SETPOINT_PROCESSOR_FLOW_LABEL_SOURCE);

    static {
        new SubflowRoute.Descriptor().initialize(SETPOINT_PROCESSOR_FLOW);
        SETPOINT_PROCESSOR_FLOW.getEditorProperties().put(EDITOR_PROPERTY_X, 300);
        SETPOINT_PROCESSOR_FLOW.getEditorProperties().put(EDITOR_PROPERTY_Y, 150);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_LABEL_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Node TEMPERATURE_LABEL = new Node("Temperature Label", new Identifier(generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE), TEMPERATURE_LABEL_SINK);

    static {
        new TextLabelNodeDescriptor().initialize(TEMPERATURE_LABEL);
        TEMPERATURE_LABEL.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:labelTemperature");
        TEMPERATURE_LABEL.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        TEMPERATURE_LABEL.getEditorProperties().put(EDITOR_PROPERTY_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_LABEL_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Node SETPOINT_LABEL = new Node("Setpoint Label", new Identifier(generateGlobalUniqueId(), TextLabelNodeDescriptor.TYPE), SETPOINT_LABEL_SINK);

    static {
        new TextLabelNodeDescriptor().initialize(SETPOINT_LABEL);
        SETPOINT_LABEL.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:labelSetpoint");
        SETPOINT_LABEL.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        SETPOINT_LABEL.getEditorProperties().put(EDITOR_PROPERTY_Y, 150);
        Widget.getWidgetDefaults(SETPOINT_LABEL).put(Widget.PROPERTY_POSITION_Y, 25);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PLUS_BUTTON_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Slot SETPOINT_PLUS_BUTTON_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK), false);
    public static Node SETPOINT_PLUS_BUTTON = new Node("Increase Temperature Button", new Identifier(generateGlobalUniqueId(), PushButtonNodeDescriptor.TYPE), SETPOINT_PLUS_BUTTON_SOURCE, SETPOINT_PLUS_BUTTON_SINK);

    static {
        new PushButtonNodeDescriptor().initialize(SETPOINT_PLUS_BUTTON);
        SETPOINT_PLUS_BUTTON.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        SETPOINT_PLUS_BUTTON.getEditorProperties().put(EDITOR_PROPERTY_Y, 400);
        Widget.getWidgetDefaults(SETPOINT_PLUS_BUTTON).put(Widget.PROPERTY_POSITION_X, 150);
        Widget.getWidgetDefaults(SETPOINT_PLUS_BUTTON).put(Widget.PROPERTY_POSITION_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_MINUS_BUTTON_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Slot SETPOINT_MINUS_BUTTON_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK), false);
    public static Node SETPOINT_MINUS_BUTTON = new Node("Decrease Temperature Button", new Identifier(generateGlobalUniqueId(), PushButtonNodeDescriptor.TYPE), SETPOINT_MINUS_BUTTON_SOURCE, SETPOINT_MINUS_BUTTON_SINK);

    static {
        new PushButtonNodeDescriptor().initialize(SETPOINT_MINUS_BUTTON);
        SETPOINT_MINUS_BUTTON.getEditorProperties().put(EDITOR_PROPERTY_X, 50);
        SETPOINT_MINUS_BUTTON.getEditorProperties().put(EDITOR_PROPERTY_Y, 500);
        Widget.getWidgetDefaults(SETPOINT_MINUS_BUTTON).put(Widget.PROPERTY_POSITION_X, 0);
        Widget.getWidgetDefaults(SETPOINT_MINUS_BUTTON).put(Widget.PROPERTY_POSITION_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PLUS_FILTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_PLUS_FILTER_TRIGGER_SINK = new Slot("Trigger", new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_PLUS_FILTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Node SETPOINT_PLUS_FILTER = new Node("Forward on trigger", new Identifier(generateGlobalUniqueId(), FilterNodeDescriptor.TYPE), SETPOINT_PLUS_FILTER_SINK, SETPOINT_PLUS_FILTER_TRIGGER_SINK, SETPOINT_PLUS_FILTER_SOURCE);

    static {
        new FilterNodeDescriptor().initialize(SETPOINT_PLUS_FILTER);
        SETPOINT_PLUS_FILTER.getProperties().put(FilterNodeDescriptor.PROPERTY_ON_TRIGGER, true);
        SETPOINT_PLUS_FILTER.getEditorProperties().put(EDITOR_PROPERTY_X, 450);
        SETPOINT_PLUS_FILTER.getEditorProperties().put(EDITOR_PROPERTY_Y, 300);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_MINUS_FILTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_MINUS_FILTER_TRIGGER_SINK = new Slot("Trigger", new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_MINUS_FILTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Node SETPOINT_MINUS_FILTER = new Node("Forward on trigger", new Identifier(generateGlobalUniqueId(), FilterNodeDescriptor.TYPE), SETPOINT_MINUS_FILTER_SINK, SETPOINT_MINUS_FILTER_TRIGGER_SINK, SETPOINT_MINUS_FILTER_SOURCE);

    static {
        new FilterNodeDescriptor().initialize(SETPOINT_MINUS_FILTER);
        SETPOINT_MINUS_FILTER.getProperties().put(FilterNodeDescriptor.PROPERTY_ON_TRIGGER, true);
        SETPOINT_MINUS_FILTER.getEditorProperties().put(EDITOR_PROPERTY_X, 450);
        SETPOINT_MINUS_FILTER.getEditorProperties().put(EDITOR_PROPERTY_Y, 450);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_INCREMENT_FUNCTION_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_INCREMENT_FUNCTION_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Node SETPOINT_INCREMENT_FUNCTION = new Node("Increment by 1", new Identifier(generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE), SETPOINT_INCREMENT_FUNCTION_SINK, SETPOINT_INCREMENT_FUNCTION_SOURCE);

    static {
        new FunctionNodeDescriptor().initialize(SETPOINT_INCREMENT_FUNCTION);
        SETPOINT_INCREMENT_FUNCTION.getProperties().put(FunctionNodeDescriptor.PROPERTY_JAVASCRIPT, "output['value'] = input + 1");
        SETPOINT_INCREMENT_FUNCTION.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        SETPOINT_INCREMENT_FUNCTION.getEditorProperties().put(EDITOR_PROPERTY_Y, 300);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_DECREMENT_FUNCTION_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_DECREMENT_FUNCTION_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SOURCE));
    public static Node SETPOINT_DECREMENT_FUNCTION = new Node("Decrement by 1", new Identifier(generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE), SETPOINT_DECREMENT_FUNCTION_SINK, SETPOINT_DECREMENT_FUNCTION_SOURCE);

    static {
        new FunctionNodeDescriptor().initialize(SETPOINT_DECREMENT_FUNCTION);
        SETPOINT_DECREMENT_FUNCTION.getProperties().put(FunctionNodeDescriptor.PROPERTY_JAVASCRIPT, "output['value'] = input - 1");
        SETPOINT_DECREMENT_FUNCTION.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        SETPOINT_DECREMENT_FUNCTION.getEditorProperties().put(EDITOR_PROPERTY_Y, 450);
    }

    /* ###################################################################################### */

    public static Slot SETPOINT_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), TYPE_SINK));
    public static Slot SETPOINT_PRODUCER_SOURCE = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), TYPE_SOURCE), false);
    public static Node SETPOINT_PRODUCER = new Node("Setpoint", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE), SETPOINT_PRODUCER_SOURCE, SETPOINT_PRODUCER_SINK);

    static {
        new ProducerRoute.Descriptor().initialize(SETPOINT_PRODUCER);
        SETPOINT_PRODUCER.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:producerSetpoint");
        SETPOINT_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_X, 1050);
        SETPOINT_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_Y, 375);
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
        new Identifier(generateGlobalUniqueId(), Flow.TYPE),
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
