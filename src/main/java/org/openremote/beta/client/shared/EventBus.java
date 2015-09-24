package org.openremote.beta.client.shared;

import org.openremote.beta.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBus.class);

    final static protected List<EventRegistration> REGISTRATIONS = new ArrayList<>();

    synchronized static public void addAll(List<EventRegistration> registrations) {
        REGISTRATIONS.addAll(registrations);
    }

    synchronized static public void removeAll(List<EventRegistration> registrations) {
        REGISTRATIONS.removeAll(registrations);
    }

    @SuppressWarnings("unchecked")
    synchronized static public void dispatch(Event event) {
        boolean vetoed = false;
        LOG.debug("Preparing event: " + event.getType());
        for (EventRegistration registration : REGISTRATIONS) {
            if (!registration.getEventType().equals(event.getType()) || !registration.isPrepare())
                continue;
            try {
                LOG.debug("Found listener for '" + registration.getEventType() + "': " + registration.getListener().getClass().getName());
                registration.getListener().on(event);
            } catch (VetoEventException ex) {
                LOG.debug("Event vetoed: " + registration.getEventType());
                vetoed = true;
                break;
            }
        }
        if (!vetoed) {
            LOG.debug("Dispatching event: " + event.getType());
            for (EventRegistration registration : REGISTRATIONS) {
                if (!registration.getEventType().equals(event.getType()) || registration.isPrepare())
                    continue;
                LOG.debug("Found listener for '" + registration.getEventType() + "': " + registration.getListener().getClass().getName());
                registration.getListener().on(event);
            }
        }
    }

}
