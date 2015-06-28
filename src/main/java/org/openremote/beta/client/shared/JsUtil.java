package org.openremote.beta.client.shared;

public class JsUtil {

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
}
