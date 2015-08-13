package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsNoExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.TimeoutHandler;
import elemental.events.CustomEvent;
import elemental.events.EventRemover;
import org.openremote.beta.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public abstract class AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPresenter.class);

    final protected Element view;

    public AbstractPresenter(com.google.gwt.dom.client.Element gwtView) {
        this.view = gwtView.cast(); // TODO No idea why this is necessary, without this we can only use superdevmode...

        if (view == null)
            throw new IllegalArgumentException("Can't instantiate presenter without a view element: " + getClass().getName());
        LOG.debug("Creating presenter for view '" + view.getLocalName() + "': " + getClass().getName());
    }

    public Element getView() {
        return view;
    }

    @JsNoExport
    public <E extends Event> EventRemover addEventListener(Class<E> eventClass,
                                                           EventListener<E> listener) {
        return addEventListener(getView(), eventClass, listener);
    }

    @JsNoExport
    public <E extends Event> EventRemover addEventListener(Element originView,
                                                           Class<E> eventClass,
                                                           EventListener<E> listener) {
        return addEventListener(originView, eventClass, false, false, listener);
    }

    @JsNoExport
    public <E extends Event> EventRemover addEventListener(Class<E> eventClass,
                                                           boolean stopPropagation,
                                                           boolean stopImmediatePropagation,
                                                           EventListener<E> listener) {
        return addEventListener(getView(), eventClass, stopPropagation, stopImmediatePropagation, listener);
    }

    @JsNoExport
    public <E extends Event> EventRemover addEventListener(Element originView,
                                                           Class<E> eventClass,
                                                           boolean stopPropagation,
                                                           boolean stopImmediatePropagation,
                                                           EventListener<E> listener) {
        String eventType = Event.getType(eventClass);
        LOG.debug("Adding event listener to view '" + originView.getLocalName() + "': " + eventType);
        return originView.addEventListener(eventType, evt -> {
            LOG.debug("Received event on view '" + originView.getLocalName() + "': " + evt.getType());
            CustomEvent customEvent = (CustomEvent) evt;
            if (stopPropagation)
                customEvent.stopPropagation();
            if (stopImmediatePropagation)
                customEvent.stopImmediatePropagation();
            @SuppressWarnings("unchecked")
            E event = (E) customEvent.getDetail();
            listener.on(event);
        }, false); // Disable the capture phase, optional bubbling is easier to understand
    }

    @JsNoExport
    public int dispatchEvent(Event event) {
        return dispatchEvent(event, 0);
    }

    @JsNoExport
    public int dispatchEvent(Element targetView, Event event) {
        return dispatchEvent(targetView, event, 0);
    }

    @JsNoExport
    public int dispatchEvent(Event event, int delayMillis) {
        return dispatchEvent(event, delayMillis, -1);
    }

    @JsNoExport
    public int dispatchEvent(Element targetView, Event event, int delayMillis) {
        return dispatchEvent(targetView, event, delayMillis, -1);
    }

    @JsNoExport
    public int dispatchEvent(Event event, int delayMillis, int timeoutId) {
        return dispatchEvent(getView(), event, delayMillis, timeoutId);
    }

    @JsNoExport
    public int dispatchEvent(Element targetView, Event event, int delayMillis, int timeoutId) {
        if (timeoutId != -1) {
            Browser.getWindow().clearTimeout(timeoutId);
        }
        TimeoutHandler dispatchHandler = () -> {
            LOG.debug("Dispatching event on view '" + targetView.getLocalName() + "': " + event.getType());
            CustomEvent customEvent = (CustomEvent) targetView.getOwnerDocument().createEvent("CustomEvent");

            boolean bubbling = true;
            boolean cancelable = true;
            if (event instanceof PropagationOptions) {
                PropagationOptions propagationOptions = (PropagationOptions) event;
                bubbling = propagationOptions.isBubbling();
                cancelable = propagationOptions.isCancelable();
            }

            customEvent.initCustomEvent(event.getType(), bubbling, cancelable, event);
            targetView.dispatchEvent(customEvent);
        };
        if (delayMillis > 0) {
            LOG.debug("Scheduling after " + delayMillis + " milliseconds, custom event on view '" + targetView.getLocalName() + "': " + event.getType());
            return Browser.getWindow().setTimeout(dispatchHandler, delayMillis);
        } else {
            dispatchHandler.onTimeoutHandler();
            return -1;
        }
    }

    @JsNoExport
    protected Element getRequiredChildView(String selector) {
        Element child = getView().querySelector(selector);
        if (child == null)
            throw new RuntimeException("Missing child view '" + selector + "' on: " + getView().getLocalName());
        return child;
    }

    @JsNoExport
    protected void addRedirectToShellView(Class<? extends Event> eventClass) {
        Element shellView = Browser.getWindow().getTop().getDocument().querySelector("or-shell");
        if (shellView == null) {
            throw new RuntimeException("Missing 'or-shell' view in browser top window document");
        }
        addEventListener(getView(), eventClass, event -> dispatchEvent(shellView, event));
    }

}

