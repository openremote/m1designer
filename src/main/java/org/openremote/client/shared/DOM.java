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

package org.openremote.client.shared;

import elemental.dom.Element;
import elemental.dom.Node;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface DOM {

    @JsProperty
    Node getParentNode();

    @JsProperty
    Node[] getChildNodes();

    @JsProperty
    Node getFirstChild();

    @JsProperty
    Node getLastChild();

    @JsProperty
    Element getFirstElementChild();

    @JsProperty
    Node getPreviousSibling();

    @JsProperty
    Node getNextSibling();

    @JsProperty
    String getTextContent();

    @JsProperty
    String getInnerHTML();

    @JsProperty
    Node[] getDistributedNodes();

    Node querySelector(String selector);

    Node[] querySelectorAll(String selector);

    void appendChild(Node node);

    void insertBefore(Node node, Node beforeNode);

    void removeChild(Node node);

    void setAttribute(String attribute, String value);

    void removeAttribute(String attribute);
}