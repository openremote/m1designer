package org.openremote.beta.server.testdata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.beta.server.catalog.change.ChangeNodeDescriptor;
import org.openremote.beta.server.catalog.function.FunctionNodeDescriptor;
import org.openremote.beta.server.catalog.storage.StorageNodeDescriptor;
import org.openremote.beta.server.route.ConsumerRoute;
import org.openremote.beta.server.route.ProducerRoute;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.io.IOException;
import java.util.List;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;

public class SampleTemperatureProcessor {

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot FAHRENHEIT_CONSUMER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node FAHRENHEIT_CONSUMER = new Node("Fahrenheit", new Identifier(generateGlobalUniqueId(), ConsumerRoute.NODE_TYPE));

    static {
        new ConsumerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(FAHRENHEIT_CONSUMER_SOURCE);
                slots.add(FAHRENHEIT_CONSUMER_SINK);
            }
        }.initialize(FAHRENHEIT_CONSUMER);
        FAHRENHEIT_CONSUMER.getEditorSettings().setPositionX((double) 10);
        FAHRENHEIT_CONSUMER.getEditorSettings().setPositionY((double) 250);
    }

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONVERTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot FAHRENHEIT_CONVERTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node FAHRENHEIT_CONVERTER = new Node("Fahrenheit to Celcius", new Identifier(generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE));

    static {
        new FunctionNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(FAHRENHEIT_CONVERTER_SINK);
                slots.add(FAHRENHEIT_CONVERTER_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return FunctionNodeDescriptor.FUNCTION_INITIAL_PROPERTIES.deepCopy()
                    .put("javascript", "output.value = (((input - 32)*5)/9).toFixed(0)");
            }
        }.initialize(FAHRENHEIT_CONVERTER);
        FAHRENHEIT_CONVERTER.getEditorSettings().setPositionX((double) 300);
        FAHRENHEIT_CONVERTER.getEditorSettings().setPositionY((double) 250);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_DATABASE_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node TEMPERATURE_DATABASE = new Node("Temperature Database", new Identifier(generateGlobalUniqueId(), StorageNodeDescriptor.TYPE));

    static {
        new StorageNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(TEMPERATURE_DATABASE_SINK);
            }
        }.initialize(TEMPERATURE_DATABASE);
        TEMPERATURE_DATABASE.setPostEndpoint("mock:temperatureDatabase");
        TEMPERATURE_DATABASE.getEditorSettings().setPositionX((double) 350);
        TEMPERATURE_DATABASE.getEditorSettings().setPositionY((double) 400);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_PRODUCER_SOURCE = new Slot("Celcius", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node CELCIUS_PRODUCER = new Node("Celcius", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE));

    static {
        new ProducerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(CELCIUS_PRODUCER_SINK);
                slots.add(CELCIUS_PRODUCER_SOURCE);
            }
        }.initialize(CELCIUS_PRODUCER);
        CELCIUS_PRODUCER.setPostEndpoint("mock:producerCelcius");
        CELCIUS_PRODUCER.getEditorSettings().setPositionX((double) 750);
        CELCIUS_PRODUCER.getEditorSettings().setPositionY((double) 250);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_APPENDER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_APPENDER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node CELCIUS_APPENDER = new Node("Append Celcius Symbol", new Identifier(generateGlobalUniqueId(), ChangeNodeDescriptor.TYPE));

    static {
        new ChangeNodeDescriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(CELCIUS_APPENDER_SINK);
                slots.add(CELCIUS_APPENDER_SOURCE);
            }

            @Override
            protected ObjectNode getInitialProperties() {
                return JSON.createObjectNode().put("append", " \u00B0C");
            }
        }.initialize(CELCIUS_APPENDER);
        CELCIUS_APPENDER.getEditorSettings().setPositionX((double) 650);
        CELCIUS_APPENDER.getEditorSettings().setPositionY((double) 400);
    }

    /* ###################################################################################### */

    public static Slot LABEL_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot LABEL_PRODUCER_SOURCE = new Slot("Label", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node LABEL_PRODUCER = new Node("Label", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE));

    static {
        new ProducerRoute.Descriptor() {
            @Override
            public void addSlots(List<Slot> slots) {
                slots.add(LABEL_PRODUCER_SINK);
                slots.add(LABEL_PRODUCER_SOURCE);
            }
        }.initialize(LABEL_PRODUCER);
        LABEL_PRODUCER.setPostEndpoint("mock:producerLabel");
        LABEL_PRODUCER.getEditorSettings().setPositionX((double) 1000);
        LABEL_PRODUCER.getEditorSettings().setPositionY((double) 450);
    }

    /* ###################################################################################### */

    public static Node[] FLOW_NODES = new Node[]{
        FAHRENHEIT_CONSUMER,
        FAHRENHEIT_CONVERTER,
        TEMPERATURE_DATABASE,
        CELCIUS_PRODUCER,
        CELCIUS_APPENDER,
        LABEL_PRODUCER
    };

    /* ###################################################################################### */

    public static Flow FLOW = new Flow(
        "Temperature Processor",
        new Identifier(generateGlobalUniqueId(), Flow.TYPE),
        FLOW_NODES,
        new Wire[]{
            new Wire(FAHRENHEIT_CONSUMER_SOURCE, FAHRENHEIT_CONVERTER_SINK),
            new Wire(FAHRENHEIT_CONSUMER_SOURCE, TEMPERATURE_DATABASE_SINK),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE, CELCIUS_PRODUCER_SINK),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE, CELCIUS_APPENDER_SINK),
            new Wire(CELCIUS_APPENDER_SOURCE, LABEL_PRODUCER_SINK)
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
