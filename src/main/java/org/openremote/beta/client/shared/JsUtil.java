package org.openremote.beta.client.shared;

import elemental.dom.Element;
import elemental.dom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsUtil.class);

    public native static void log(Object o) /*-{
        console.dir(o);
    }-*/;

    public native static String randomUUID() /*-{
        var s = [];
        for (var i = 0; i < 36; i++) s.push(Math.floor(Math.random() * 10));
        return s.join('');
    }-*/;

    public native static void wait(int millis, Runnable r) /*-{
        var runnable = function () {
            r.@java.lang.Runnable::run(*)();
        };
        setTimeout(runnable, millis);
    }-*/;

    public native static int compare(String a, String b) /*-{
        return a.localeCompare(b);
    }-*/;


    static public native Component.DOM dom(Node n) /*-{
        return $wnd.Polymer.dom(n);
    }-*/;

    static public native Component.DOM domRoot(Element e) /*-{
        return $wnd.Polymer.dom(e.root);
    }-*/;
}
