package org.openremote.client.shared;

import org.openremote.shared.event.Event;

public class EventRegistration<E extends Event> {

    final public boolean prepare;
    final public Class<E> eventClass;
    final public EventListener<E> listener;

    public EventRegistration(boolean prepare, Class<E> eventClass, EventListener<E> listener) {
        this.prepare = prepare;
        this.eventClass = eventClass;
        this.listener = listener;
    }

    public boolean isPrepare() {
        return prepare;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public String getEventType() {
        return Event.getType(getEventClass());
    }

    public EventListener<E> getListener() {
        return listener;
    }
}
