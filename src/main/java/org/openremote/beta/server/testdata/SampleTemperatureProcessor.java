package org.openremote.beta.server.testdata;

import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;

import java.util.Map;

import static org.openremote.beta.server.testdata.SampleColors.*;
import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.shared.util.Util.createMap;

public class SampleTemperatureProcessor {

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONSUMER_SOURCE = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
    public static Slot FAHRENHEIT_CONSUMER_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, false);
    public static Node FAHRENHEIT_CONSUMER = new Node(generateGlobalUniqueId(), "Consumer", "Fahrenheit", FAHRENHEIT_CONSUMER_SOURCE, FAHRENHEIT_CONSUMER_SINK);

    static {
        Map<String, Object> properties = createMap();
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", VIRTUAL_COLOR);
        editor.put("x", 10);
        editor.put("y", 50);
        FAHRENHEIT_CONSUMER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot FAHRENHEIT_CONVERTER_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
    public static Slot FAHRENHEIT_CONVERTER_SOURCE = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
    public static Node FAHRENHEIT_CONVERTER = new Node(generateGlobalUniqueId(), "Function", "Fahrenheit to Celcius", FAHRENHEIT_CONVERTER_SINK, FAHRENHEIT_CONVERTER_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        properties.put("javascript", "output['value'] = ((input - 32)*5)/9");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", PROCESSOR_COLOR);
        editor.put("x", 400);
        editor.put("y", 80);
        FAHRENHEIT_CONVERTER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot TEMPERATURE_DATABASE_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
    public static Node TEMPERATURE_DATABASE = new Node(generateGlobalUniqueId(), "Storage", "Temperature Database", TEMPERATURE_DATABASE_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("postEndpoint", "mock:temperatureDatabase");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", PROCESSOR_COLOR);
        editor.put("x", 350);
        editor.put("y", 200);
        TEMPERATURE_DATABASE.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_PRODUCER_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
    public static Node CELCIUS_PRODUCER = new Node(generateGlobalUniqueId(), "Producer", "Celcius", CELCIUS_PRODUCER_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("postEndpoint", "mock:producerCelcius");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", VIRTUAL_COLOR);
        editor.put("x", 750);
        editor.put("y", 50);
        CELCIUS_PRODUCER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot CELCIUS_APPENDER_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
    public static Slot CELCIUS_APPENDER_SOURCE = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
    public static Node CELCIUS_APPENDER = new Node(generateGlobalUniqueId(), "Change", "Append Celcius Symbol", CELCIUS_APPENDER_SINK, CELCIUS_APPENDER_SOURCE);

    static {
        Map<String, Object> properties = createMap();
        properties.put("append", " C");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", PROCESSOR_COLOR);
        editor.put("x", 650);
        editor.put("y", 200);
        CELCIUS_APPENDER.setProperties(properties);
    }

    /* ###################################################################################### */

    public static Slot LABEL_PRODUCER_SINK = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
    public static Node LABEL_PRODUCER = new Node(generateGlobalUniqueId(), "Producer", "Label", LABEL_PRODUCER_SINK);

    static {
        Map<String, Object> properties = createMap();
        properties.put("postEndpoint", "mock:producerLabel");
        Map<String, Object> editor = createMap(properties, "editor");
        editor.put("color", VIRTUAL_COLOR);
        editor.put("x", 1000);
        editor.put("y", 250);
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

    public static Flow FLOW = new Flow(generateGlobalUniqueId(), "Temperature Processor", FLOW_NODES,
        new Wire[]{
            new Wire(FAHRENHEIT_CONSUMER_SOURCE.getId(), FAHRENHEIT_CONVERTER_SINK.getId()),
            new Wire(FAHRENHEIT_CONSUMER_SOURCE.getId(), TEMPERATURE_DATABASE_SINK.getId()),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE.getId(), CELCIUS_PRODUCER_SINK.getId()),
            new Wire(FAHRENHEIT_CONVERTER_SOURCE.getId(), CELCIUS_APPENDER_SINK.getId()),
            new Wire(CELCIUS_APPENDER_SOURCE.getId(), LABEL_PRODUCER_SINK.getId())
        }
    );
}
