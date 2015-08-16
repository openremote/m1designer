package org.openremote.beta.server.testdata;

import org.openremote.beta.server.catalog.change.ChangeNodeDescriptor;
import org.openremote.beta.server.catalog.function.FunctionNodeDescriptor;
import org.openremote.beta.server.catalog.storage.StorageNodeDescriptor;
import org.openremote.beta.server.route.ActuatorRoute;
import org.openremote.beta.server.route.ConsumerRoute;
import org.openremote.beta.server.route.ProducerRoute;
import org.openremote.beta.shared.flow.*;
import org.openremote.beta.shared.model.Identifier;
import org.openremote.beta.shared.model.Properties;

import java.io.IOException;
import java.util.Map;

import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.server.util.JsonUtil.JSON;

public class SampleTemperatureProcessor {

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONSUMER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Slot FAHRENHEIT_CONSUMER_SINK = new Slot("Fahrenheit", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK), false);
    public static Node FAHRENHEIT_CONSUMER = new Node("Fahrenheit", new Identifier(generateGlobalUniqueId(), ConsumerRoute.NODE_TYPE), FAHRENHEIT_CONSUMER_SOURCE, FAHRENHEIT_CONSUMER_SINK);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("clientAccess", "true");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 10);
        editor.put("y", 50);
        editor.put("color", NodeColor.VIRTUAL);
        editor.put("typeLabel", ConsumerRoute.NODE_TYPE_LABEL);
        FAHRENHEIT_CONSUMER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONVERTER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot FAHRENHEIT_CONVERTER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node FAHRENHEIT_CONVERTER = new Node("Fahrenheit to Celcius", new Identifier(generateGlobalUniqueId(), FunctionNodeDescriptor.TYPE), FAHRENHEIT_CONVERTER_SINK, FAHRENHEIT_CONVERTER_SOURCE);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("javascript", "output['value'] = ((input - 32)*5)/9");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 400);
        editor.put("y", 80);
        editor.put("color", NodeColor.DEFAULT);
        editor.put("typeLabel", FunctionNodeDescriptor.TYPE_LABEL);
        FAHRENHEIT_CONVERTER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_DATABASE_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Node TEMPERATURE_DATABASE = new Node("Temperature Database", new Identifier(generateGlobalUniqueId(), StorageNodeDescriptor.TYPE), TEMPERATURE_DATABASE_SINK);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("postEndpoint", "mock:temperatureDatabase");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 350);
        editor.put("y", 200);
        editor.put("color", NodeColor.DEFAULT);
        editor.put("typeLabel", StorageNodeDescriptor.TYPE_LABEL);
        TEMPERATURE_DATABASE.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_PRODUCER_SOURCE = new Slot("Celcius", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node CELCIUS_PRODUCER = new Node("Celcius", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE), CELCIUS_PRODUCER_SINK, CELCIUS_PRODUCER_SOURCE);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:producerCelcius");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 750);
        editor.put("y", 50);
        editor.put("color", NodeColor.VIRTUAL);
        editor.put("typeLabel", ProducerRoute.NODE_TYPE_LABEL);
        CELCIUS_PRODUCER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_APPENDER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot CELCIUS_APPENDER_SOURCE = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
    public static Node CELCIUS_APPENDER = new Node("Append Celcius Symbol", new Identifier(generateGlobalUniqueId(), ChangeNodeDescriptor.TYPE), CELCIUS_APPENDER_SINK, CELCIUS_APPENDER_SOURCE);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("append", " C");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 650);
        editor.put("y", 200);
        editor.put("color", NodeColor.DEFAULT);
        editor.put("typeLabel", ChangeNodeDescriptor.TYPE_LABEL);
        CELCIUS_APPENDER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LABEL_PRODUCER_SINK = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
    public static Slot LABEL_PRODUCER_SOURCE = new Slot("Label", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE), false);
    public static Node LABEL_PRODUCER = new Node("Label", new Identifier(generateGlobalUniqueId(), ProducerRoute.NODE_TYPE), LABEL_PRODUCER_SINK, LABEL_PRODUCER_SOURCE);

    static {
        Map<String, Object> properties = Properties.create();
        properties.put("clientAccess", "true");
        properties.put("postEndpoint", "mock:producerLabel");
        Map<String, Object> editor = Properties.create(properties, "editor");
        editor.put("x", 1000);
        editor.put("y", 250);
        editor.put("color", NodeColor.VIRTUAL);
        editor.put("typeLabel", ProducerRoute.NODE_TYPE_LABEL);
        LABEL_PRODUCER.setProperties(properties);
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
