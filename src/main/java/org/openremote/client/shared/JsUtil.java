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
import elemental.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Evil monstrous hacks
public class JsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsUtil.class);

    public native static void log(Object o) /*-{
        console.dir(o);
    }-*/;

    public native static int compare(String a, String b) /*-{
        return a.localeCompare(b);
    }-*/;

    static public native DOM dom(View v) /*-{
        return $wnd.Polymer.dom(v);
    }-*/;

    static public native DOM domRoot(View v) /*-{
        return $wnd.Polymer.dom(v.root);
    }-*/;

    static public native View host(View v) /*-{
        return v.host;
    }-*/;

    static public native View createView(View v, String elementName) /*-{
        return v.ownerDocument.createElement(elementName);
    }-*/;

    static public native NodeList querySelectorAll(View v, String selector) /*-{
        return v.querySelectorAll(selector);
    }-*/;

    static public native com.google.gwt.dom.client.Element asGwtElement(Object o) /*-{
        return o;
    }-*/;

    static public native Element asElementalElement(Object o) /*-{
        return o;
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
