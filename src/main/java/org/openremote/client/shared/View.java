package org.openremote.client.shared;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface View {

    @JsProperty
    String getLocalName();

    Object $$(String selector);

    Object fire(String type, Object detail);

    Object get(String path);

    Object get(String[] paths);

    Object get(String path, Object root);

    Object get(String[] paths, Object root);

    void set(String path, Object value);

    void set(String path, Object value, Object root);

    void set(String path, boolean value);

    void set(String path, boolean value, Object root);

    void toggleAttribute(String name, boolean b, Object node);

    void toggleClass(String name, boolean b, Object node);

}
