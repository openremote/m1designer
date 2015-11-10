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

package org.openremote.server.util;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This should have been part of the JDK.
 * <p>
 * The XPath API needs a map to lookup namespace URIs using prefix keys. Unfortunately,
 * the authors did not know <tt>java.util.Map</tt>.
 * </p>
 */
public class NamespaceContextMap extends HashMap<String, String> implements NamespaceContext {

    public static class Mapping {
        public String prefix;
        public String namespace;

        public Mapping(String prefix, String namespace) {
            this.prefix = prefix;
            this.namespace = namespace;
        }
    }


    protected String defaultNamespace = XMLConstants.NULL_NS_URI;

    public NamespaceContextMap() {
    }

    public NamespaceContextMap(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public NamespaceContextMap(Mapping... mappings) {
        this(XMLConstants.NULL_NS_URI, mappings);
    }

    public NamespaceContextMap(String defaultNamespace, Mapping... mappings) {
        this.defaultNamespace = defaultNamespace;
        if (mappings != null) {
            for (Mapping mapping : mappings) {
                put(mapping.prefix, mapping.namespace);
            }
        }
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }
        else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }
        else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return getDefaultNamespace();
        }
        else if (containsKey(prefix)) {
            return get(prefix);
        }
        else {
            return XMLConstants.NULL_NS_URI;
        }
    }

    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException("We thought this would never be called, so we didn't implement it.");
    }

    public Iterator getPrefixes(String s) {
        throw new UnsupportedOperationException("We thought this would never be called, so we didn't implement it.");
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }
}
