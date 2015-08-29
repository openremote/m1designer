package org.openremote.beta.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.openremote.beta.server.catalog.widget.TextLabelNodeDescriptor;
import org.openremote.beta.shared.catalog.CatalogItem;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.NodeColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.openremote.beta.server.util.JsonUtil.JSON;

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
        assertNull(textLabelNode.getLabel());
        assertEquals(textLabelNode.getIdentifier().getType(), TextLabelNodeDescriptor.TYPE);

        assertTrue(textLabelNode.isClientAccess());
        assertTrue(textLabelNode.isClientWidget());
        assertEquals(textLabelNode.getEditorSettings().getNodeColor(), NodeColor.CLIENT);
        assertNotNull(textLabelNode.getEditorSettings().getTypeLabel());
        assertTrue(textLabelNode.getPersistentPropertyPaths().length > 0);
        ObjectNode textLabelProperties = JSON.readValue(textLabelNode.getProperties(), ObjectNode.class);
        assertEquals(textLabelProperties, TextLabelNodeDescriptor.TEXT_LABEL_INITIAL_PROPERTIES);
    }

}
