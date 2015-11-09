package org.openremote.client.shared;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import org.openremote.shared.func.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.gwt.user.client.Event.*;

public class LongPressListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(LongPressListener.class);

    final protected Element element;
    final protected Callback callback;
    final protected int longPressDelayMillis;
    protected Timer longPressTimer;

    public LongPressListener(View view, int longPressDelayMillis, Callback callback) {
        this(JsUtil.asGwtElement(view), longPressDelayMillis, callback);
    }

    public LongPressListener(Element element, int longPressDelayMillis, Callback callback) {
        this.element = element;
        this.longPressDelayMillis = longPressDelayMillis;
        this.callback = callback;

        Event.sinkEvents(element, ONTOUCHSTART | ONTOUCHCANCEL | ONTOUCHMOVE | ONTOUCHEND | ONMOUSEDOWN | ONMOUSEMOVE | ONMOUSEUP);
        Event.setEventListener(element, this);
    }

    @Override
    public void onBrowserEvent(Event event) {
        Element target = Element.as(event.getEventTarget());
        if (!target.equals(element))
            return;

        if (event.getTypeInt() == ONTOUCHSTART
            || event.getTypeInt() == ONMOUSEDOWN) {
            if (longPressTimer == null && callback != null) {
                longPressTimer = new Timer() {
                    @Override
                    public void run() {
                        target.getStyle().setOpacity(1.0);
                        if (callback != null)
                            callback.call();
                    }
                };
                target.getStyle().setOpacity(0.9);
                longPressTimer.schedule(longPressDelayMillis);
            }
        } else if (event.getTypeInt() == ONTOUCHEND
            || event.getTypeInt() == ONTOUCHCANCEL
            || event.getTypeInt() == ONMOUSEUP) {
            if (longPressTimer != null) {
                target.getStyle().setOpacity(1.0);
                longPressTimer.cancel();
                longPressTimer = null;
            }
        }
    }
}