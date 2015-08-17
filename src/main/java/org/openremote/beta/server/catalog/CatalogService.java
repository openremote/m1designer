package org.openremote.beta.server.catalog;

import org.apache.camel.CamelContext;
import org.apache.camel.StaticService;
import org.openremote.beta.shared.catalog.CatalogCategory;
import org.openremote.beta.shared.catalog.CatalogItem;
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
        Set<NodeDescriptor> nodeDescriptorSet = context.getRegistry().findByType(NodeDescriptor.class);
        for (NodeDescriptor nodeDescriptor : nodeDescriptorSet) {

            if (nodeDescriptor.isInternal())
                continue;

            CatalogCategory catalogCategory;
            if (nodeDescriptor instanceof WidgetNodeDescriptor) {
                catalogCategory = CatalogCategory.WIDGETS;
            } else if (nodeDescriptor instanceof VirtualNodeDescriptor) {
                catalogCategory = CatalogCategory.VIRTUAL;
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

    @Override
    public void stop() throws Exception {
        catalogItems.clear();
    }

    public CatalogItem[] getItems() {
        LOG.debug("Getting catalog items");
        return catalogItems.toArray(new CatalogItem[catalogItems.size()]);
    }

}
