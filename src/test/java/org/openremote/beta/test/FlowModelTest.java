package org.openremote.beta.test;

import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FlowModelTest {

    @Test
    public void duplicateWires() throws Exception {
        Flow flow = new Flow();
        flow.addWire(new Wire("a", "b"));
        flow.addWire(new Wire("a", "b"));
        Assert.assertEquals(flow.getWires().length, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void duplicateWiresException() throws Exception {
        new Flow("foo", new Identifier("123"), new Node[0], new Wire[]{new Wire("a", "b"), new Wire("a", "b")});
    }
}
