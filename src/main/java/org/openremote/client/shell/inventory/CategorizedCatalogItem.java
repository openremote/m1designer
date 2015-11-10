/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.client.shell.inventory;

import jsinterop.annotations.JsType;
import org.openremote.shared.catalog.CatalogCategory;
import org.openremote.shared.catalog.CatalogItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsType
public class CategorizedCatalogItem {

    public CatalogCategory category;
    public CatalogItem[] catalogItems = new CatalogItem[0];

    public CategorizedCatalogItem(CatalogCategory category) {
        this.category = category;
    }

    public void addCatalogItem(CatalogItem catalogItem) {
        List<CatalogItem> list = new ArrayList<>();
        list.addAll(Arrays.asList(catalogItems));
        list.add(catalogItem);
        catalogItems = list.toArray(new CatalogItem[list.size()]);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "category=" + category +
            ", catalogItems=" + Arrays.toString(catalogItems) +
            '}';
    }
}
