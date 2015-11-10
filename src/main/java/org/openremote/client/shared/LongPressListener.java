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