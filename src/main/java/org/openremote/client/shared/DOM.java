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