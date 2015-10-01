package org.openremote.client.shared;

import elemental.dom.Element;
import elemental.dom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsUtil.class);

    public native static void log(Object o) /*-{
        console.dir(o);
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

    static public native Component host(Node n) /*-{
        return n.host;
    }-*/;

    static public native void pushArray(String array, Object obj) /*-{
        this.@AbstractPresenter::view.push("_presenter." + array, obj);
    }-*/;

    static public native String getQueryArgument(String parameter) /*-{
        var query = $wnd.location.search.substring(1);
        var vars = query.split("&");
        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            if (pair[0] == parameter) {
                return decodeURIComponent(pair[1]);
            }
        }
        return undefined;
    }-*/;




}
