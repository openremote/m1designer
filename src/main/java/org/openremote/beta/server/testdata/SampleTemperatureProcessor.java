package org.openremote.beta.server.testdata;

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

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;
import static org.openremote.beta.shared.flow.Node.*;

public class SampleTemperatureProcessor {

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot FAHRENHEIT_CONSUMER_SINK = new Slot("Fahrenheit", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node FAHRENHEIT_CONSUMER = new Node("Fahrenheit", new Identifier(generateGlobalUniqueId(), ConsumerRoute.NODE_TYPE), FAHRENHEIT_CONSUMER_SOURCE, FAHRENHEIT_CONSUMER_SINK);

    static {
        new ConsumerRoute.Descriptor().initialize(FAHRENHEIT_CONSUMER);
        FAHRENHEIT_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_X, 10);
        FAHRENHEIT_CONSUMER.getEditorProperties().put(EDITOR_PROPERTY_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONVERTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot FAHRENHEIT_CONVERTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node FAHRENHEIT_CONVERTER = new Node("Fahrenheit to Celcius", new Identifier(generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE), FAHRENHEIT_CONVERTER_SINK, FAHRENHEIT_CONVERTER_SOURCE);

    static {
        new FunctionNodeDescriptor().initialize(FAHRENHEIT_CONVERTER);
        FAHRENHEIT_CONVERTER.getProperties().put(FunctionNodeDescriptor.PROPERTY_JAVASCRIPT, "output['value'] = ((input - 32)*5)/9");
        FAHRENHEIT_CONVERTER.getEditorProperties().put(EDITOR_PROPERTY_X, 400);
        FAHRENHEIT_CONVERTER.getEditorProperties().put(EDITOR_PROPERTY_Y, 80);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_DATABASE_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node TEMPERATURE_DATABASE = new Node("Temperature Database", new Identifier(generateGlobalUniqueId(), StorageNodeDescriptor.TYPE), TEMPERATURE_DATABASE_SINK);

    static {
        new StorageNodeDescriptor().initialize(TEMPERATURE_DATABASE);
        TEMPERATURE_DATABASE.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:temperatureDatabase");
        TEMPERATURE_DATABASE.getEditorProperties().put(EDITOR_PROPERTY_X, 350);
        TEMPERATURE_DATABASE.getEditorProperties().put(EDITOR_PROPERTY_Y, 200);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_PRODUCER_SOURCE = new Slot("Celcius", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node CELCIUS_PRODUCER = new Node("Celcius", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE), CELCIUS_PRODUCER_SINK, CELCIUS_PRODUCER_SOURCE);

    static {
        new ProducerRoute.Descriptor().initialize(CELCIUS_PRODUCER);
        CELCIUS_PRODUCER.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:producerCelcius");
        CELCIUS_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_X, 750);
        CELCIUS_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_Y, 50);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_APPENDER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_APPENDER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node CELCIUS_APPENDER = new Node("Append Celcius Symbol", new Identifier(generateGlobalUniqueId(), ChangeNodeDescriptor.TYPE), CELCIUS_APPENDER_SINK, CELCIUS_APPENDER_SOURCE);

    static {
        new ChangeNodeDescriptor().initialize(CELCIUS_APPENDER);
        CELCIUS_APPENDER.getProperties().put(ChangeNodeDescriptor.PROPERTY_APPEND, " C");
        CELCIUS_APPENDER.getEditorProperties().put(EDITOR_PROPERTY_X, 650);
        CELCIUS_APPENDER.getEditorProperties().put(EDITOR_PROPERTY_Y, 200);
    }

    /* ###################################################################################### */

    public static Slot LABEL_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot LABEL_PRODUCER_SOURCE = new Slot("Label", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node LABEL_PRODUCER = new Node("Label", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE), LABEL_PRODUCER_SINK, LABEL_PRODUCER_SOURCE);

    static {
        new ProducerRoute.Descriptor().initialize(LABEL_PRODUCER);
        LABEL_PRODUCER.getProperties().put(PROPERTY_POST_ENDPOINT, "mock:producerLabel");
        LABEL_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_X, 1000);
        LABEL_PRODUCER.getEditorProperties().put(EDITOR_PROPERTY_Y, 250);
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
