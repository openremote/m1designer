package org.openremote.server.catalog;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.catalog.CatalogItem;
import org.openremote.shared.flow.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CatalogService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogService.class);

    final protected CamelContext context;
    final protected List<CatalogItem> catalogItems = new ArrayList<>();

    public CatalogService(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
        synchronized (catalogItems) {
            Set<NodeDescriptor> nodeDescriptorSet = context.getRegistry().findByType(NodeDescriptor.class);
            for (NodeDescriptor nodeDescriptor : nodeDescriptorSet) {

                if (nodeDescriptor.isInternal())
                    continue;

                // TODO this type discrimination is not pretty
                CatalogCategory catalogCategory;
                if (nodeDescriptor.getType().startsWith("urn:openremote:widget:")) {
                    catalogCategory = CatalogCategory.WIDGETS;
                } else if (nodeDescriptor instanceof VirtualNodeDescriptor) {
                    catalogCategory = CatalogCategory.WIRING;
                } else {
                    catalogCategory = CatalogCategory.PROCESSORS;
                }

                CatalogItem catalogItem = new CatalogItem(
                    nodeDescriptor.getTypeLabel(),
                    catalogCategory,
                    nodeDescriptor.getType(),
                    nodeDescriptor.getColor()
                );

                LOG.debug("Adding catalog item: " + catalogItem);
                catalogItems.add(catalogItem);
            }

            catalogItems.sort((o1, o2) -> o1.getLabel().compareTo(o2.getLabel()));
        }
    }

    @Override
    public void stop() throws Exception {
        synchronized (catalogItems) {
            catalogItems.clear();
        }
    }

    public CatalogItem[] getItems() {
        LOG.debug("Getting catalog items");
        synchronized (catalogItems) {
            return catalogItems.toArray(new CatalogItem[catalogItems.size()]);
        }
    }

    public Node getNewNode(@Header("type") String nodeType) {
        LOG.debug("Getting new node of type: " + nodeType);
        NodeDescriptor nodeDescriptor = context.getRegistry().lookupByNameAndType(nodeType, NodeDescriptor.class);
        if (nodeDescriptor == null)
            return null;
        return nodeDescriptor.createNode();
    }

}
