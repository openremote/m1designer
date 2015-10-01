package org.openremote.test;

import org.openremote.shared.model.Properties;
import org.openremote.shared.model.Property;
import org.openremote.shared.model.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Map;

import static org.openremote.server.util.JsonUtil.JSON;
import static org.testng.Assert.*;

public class PropertiesTest {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesTest.class);

    @Test
    public void descriptors() throws Exception {

        PropertyDescriptor<String> fooDescriptor = new PropertyDescriptor.StringType("Foo", "This is foo");
        PropertyDescriptor<Long> barDescriptor = new PropertyDescriptor.LongType("Bar", "This is bar");
        PropertyDescriptor<Boolean> bazDescriptor = new PropertyDescriptor.BooleanType("Baz", "This is baz");
        PropertyDescriptor<Integer> abcDescriptor = new PropertyDescriptor.IntegerType("Abc", "This is abc");
        PropertyDescriptor<Double> someDescriptor = new PropertyDescriptor.DoubleType("Some", "This is some other value");

        Map<String, Object> properties = Properties.create();
        properties.put("foo", "FOO");
        properties.put("bar", 123l);
        properties.put("baz", true);

        Map<String, Object> nested = Properties.create(properties, "nested");
        nested.put("abc", 456);

        Map<String, Object> deeper = Properties.create(nested, "deeper");
        deeper.put("some", (double) 111);

        String propertiesString = JSON.writeValueAsString(properties);
        properties = JSON.readValue(propertiesString, Map.class);

        assertEquals(properties.size(), 4);
        assertEquals(
            Properties.get(properties, fooDescriptor, "foo"),
            "FOO"
        );
        assertEquals(
            Properties.get(properties, barDescriptor, "bar"),
            new Long(123l)
        );
        assertTrue(
            Properties.isTrue(properties, bazDescriptor, "baz")
        );
        assertFalse(
            Properties.containsProperties(properties, "foo")
        );

        assertTrue(
            Properties.containsProperties(properties, "nested")
        );
        nested = Properties.getProperties(properties, "nested");
        assertEquals(nested.size(), 2);
        assertEquals(
            Properties.get(nested, abcDescriptor, "abc"),
            new Integer(456)
        );

        deeper = Properties.getProperties(nested, "deeper");
        assertEquals(deeper.size(), 1);
        assertEquals(
            Properties.get(deeper, someDescriptor, "some"),
            (double) 111
        );
    }

    @Test
    public void property() throws Exception {
        Property<String> foo = new Property<>(new PropertyDescriptor.StringType("Foo", "This is foo"), "FOO");

        String json = JSON.writeValueAsString(foo);
        foo = JSON.readValue(json, Property.class);

        assertEquals(foo.getValue(), "FOO");
    }


}
