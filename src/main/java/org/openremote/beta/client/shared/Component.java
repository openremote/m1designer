package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsProperty;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.js.util.JsArrayOf;

@JsType
public interface Component {

    static int push(Component component, String path, Object... items) {
        int count = 0;
        if (items != null)
            for (Object item : items) {
                count += component.push(path, item);
            }
        return count;
    }

    static int unshift(Component component, String path, Object... items) {
        int count = 0;
        if (items != null)
            for (Object item : items) {
                count += component.unshift(path, item);
            }
        return count;
    }

    @JsType
    interface DOM {

        @JsType
        interface ClassList {
            void add(String cssClass);

            void add(String[] cssClasses);

            void remove(String cssClass);

            void remove(String[] cssClasses);

            void toggle(String cssClass, boolean b);

            boolean contains(String cssClass);

            String[] values();
        }

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
        Element getLastElementChild();

        @JsProperty
        Node getPreviousSibling();

        @JsProperty
        Node getNextSibling();

        @JsProperty
        String getTextContent();

        @JsProperty
        String getInnerHTML();

        @JsProperty
        ClassList getClassList();

        Node[] getDistributedNodes();

        Object getDestinationInsertionPoints();

        Node querySelector(String selector);

        Node[] querySelectorAll(String selector);

        void appendChild(Node node);

        void insertBefore(Node node, Node beforeNode);

        void removeChild(Node node);

        void setAttribute(String attribute, String value);

        void removeAttribute(String attribute);
    }

    @JsProperty
    Component getRoot();

    Element $$(String selector);

    <T> T[] arrayDelete(T[] array, T item);

    <T> T[] arrayDelete(String path, T item);

    int async(Function function, int waitTime);

    void attributeFollows(String name, Element toElement, Element fromElement);

    void cancelAsync(int handle);

    void cancelDebouncer(String jobName);

    void classFollows(String name, Element toElement, Element fromElement);

    Element create(String tag, Object props);

    void debounce(String jobName, Function callback, int wait);

    void distributeContent(boolean updateInsertionPoints);

    void distributeContent();

    Element domHost();

    boolean elementMatches(String selector, Node node);

    CustomEvent fire(String type);

    CustomEvent fire(String type, Object detail);

    CustomEvent fire(String type, Object detail, Object options);

    void flushDebouncer(String jobName);

    Object get(String path);

    Object get(String[] paths);

    Object get(String path, Object root);

    Object get(String[] paths, Object root);

    Node[] getContentChildNodes(String selector);

    Element getContentChildren(String selector);

    Object getNativePrototype(String tag);

    Object getPropertyInfo(String property);

    boolean isDebouncerActive(String jobName);

    void linkPaths(String to, String from);

    void listen(Node node, String eventName, String methodName);

    boolean notifyPath(String path, Object value);

    boolean notifyPath(String path, Object value, boolean fromAbove);

    Object pop(String path);

    int push(String path, Object item);

    String resolveUrl(String url);

    void scopeSubtree(Node container, boolean shouldObserve);

    void set(String path, Object value);

    void set(String path, Object value, Object root);

    void set(String path, boolean value);

    void set(String path, boolean value, Object root);

    void setScrollDirection(String direction, Node node);

    Object shift(String path);

    JsArrayOf splice(String path, int start, int deleteCount);

    void toggleAttribute(String name, boolean b, Node node);

    void toggleClass(String name, boolean b, Node node);

    void transform(String transform, Element element);

    void translate3d(int x, int y, int z, Element element);

    void unlinkPaths(String path);

    void unlisten(Node node, String eventName, String methodName);

    int unshift(String path, Object item);

    void updateStyles(Object properties);

}
