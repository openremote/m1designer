package org.openremote.test;

import org.openremote.shared.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UtilTest {

    private static final Logger LOG = LoggerFactory.getLogger(UtilTest.class);

    @Test
    public void camelCaseConversion() throws Exception {
        assertEquals(Util.toLowerCaseDash("EXFooBar123X"), "ex-foo-bar-123x");
        assertEquals(Util.toLowerCaseDash("EXFooBar123"), "ex-foo-bar-123");
        assertEquals(Util.toLowerCaseDash("fooX"), "foo-x");
        assertEquals(Util.toLowerCaseDash("XFoo"), "x-foo");
        assertEquals(Util.toLowerCaseDash("Xfoo"), "xfoo");

    }

}
