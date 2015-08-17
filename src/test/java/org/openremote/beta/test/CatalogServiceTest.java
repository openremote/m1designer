package org.openremote.beta.test;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.shared.catalog.CatalogItem;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.openremote.beta.shared.model.Properties;
import org.openremote.beta.shared.model.PropertyDescriptor;
import org.openremote.beta.shared.widget.TextLabel;
import org.openremote.beta.shared.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.shared.model.PropertyDescriptor.TYPE_INTEGER;

public class CatalogServiceTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceTest.class);

    @Produce
    ProducerTemplate producerTemplate;

    @Test
    public void getItemsCreateNode() throws Exception {
        CatalogItem[] catalogItems = fromJson(
            producerTemplate.requestBody(createWebClientUri("catalog"), null, String.class),
            CatalogItem[].class
        );
        assertTrue(catalogItems.length > 0);

        CatalogItem textLabelItem = null;
        for (CatalogItem item : catalogItems) {
            if (item.getNodeType().equals(TextLabelNodeDescriptor.TYPE)) {
                textLabelItem = item;
                break;
            }
        }

        assertNotNull(textLabelItem);

        Node textLabelNode = fromJson(
            producerTemplate.requestBody(createWebClientUri("catalog", "node", textLabelItem.getNodeType()), null, String.class),
            Node.class
        );

        assertNotNull(textLabelNode.getId());
        assertNotNull(textLabelNode.getLabel());
        assertEquals(textLabelNode.getIdentifier().getType(), TextLabelNodeDescriptor.TYPE);

        assertTrue(Properties.isTrue(textLabelNode.getProperties(), Node.PROPERTY_CLIENT_ACCESS));
        assertEquals(Properties.get(textLabelNode.getEditorProperties(), Node.EDITOR_PROPERTY_COLOR), NodeColor.CLIENT.name());
        assertTrue(Properties.isSet(textLabelNode.getEditorProperties(), Node.EDITOR_PROPERTY_TYPE_LABEL));

        assertEquals(Properties.get(Widget.getWidgetProperties(textLabelNode), Widget.PROPERTY_COMPONENT), TextLabel.COMPONENT);
        assertEquals(Properties.get(Widget.getWidgetDefaults(textLabelNode), TYPE_INTEGER, Widget.PROPERTY_POSITION_X), new Integer(0));
        assertEquals(Properties.get(Widget.getWidgetDefaults(textLabelNode), TYPE_INTEGER, Widget.PROPERTY_POSITION_Y), new Integer(0));
    }

}
