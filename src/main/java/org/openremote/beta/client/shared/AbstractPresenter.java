package org.openremote.beta.client.shared;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.TimeoutHandler;
import elemental.events.CustomEvent;
import elemental.events.EventRemover;
import elemental.html.IFrameElement;
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

    public <E extends Event> EventRemover addEventListener(Class<E> eventClass,
                                                           EventListener<E> listener) {
        return addEventListenerOnView(getView(), eventClass, listener);
    }

    public <E extends Event> EventRemover addEventListenerOnView(Element originView,
                                                                 Class<E> eventClass,
                                                                 EventListener<E> listener) {
        return addEventListenerOnViewWithOptions(originView, eventClass, false, false, listener);
    }

    public <E extends Event> EventRemover addEventListenerWithOptions(Class<E> eventClass,
                                                                      boolean stopPropagation,
                                                                      boolean stopImmediatePropagation,
                                                                      EventListener<E> listener) {
        return addEventListenerOnViewWithOptions(getView(), eventClass, stopPropagation, stopImmediatePropagation, listener);
    }

    public <E extends Event> EventRemover addEventListenerOnViewWithOptions(Element originView,
                                                                            Class<E> eventClass,
                                                                            boolean stopPropagation,
                                                                            boolean stopImmediatePropagation,
                                                                            EventListener<E> listener) {
        String eventType = Event.getType(eventClass);
        LOG.debug("Adding event listener to view '" + originView.getLocalName() + "': " + eventType);
        return originView.addEventListener(eventType, evt -> {
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

    public int dispatchEvent(Event event) {
        return dispatchEventDelay(event, 0);
    }

    public int dispatchEventOnView(Element targetView, Event event) {
        return dispatchEventOnViewDelay(targetView, event, 0);
    }

    public int dispatchEventDelay(Event event, int delayMillis) {
        return dispatchEventDelayCancelExisting(event, delayMillis, -1);
    }

    public int dispatchEventOnViewDelay(Element targetView, Event event, int delayMillis) {
        return dispatchEventOnViewDelayCancelExisting(targetView, event, delayMillis, -1);
    }

    public int dispatchEventDelayCancelExisting(Event event, int delayMillis, int timeoutId) {
        return dispatchEventOnViewDelayCancelExisting(getView(), event, delayMillis, timeoutId);
    }

    public int dispatchEventOnViewDelayCancelExisting(Element targetView, Event event, int delayMillis, int timeoutId) {
        if (timeoutId != -1) {
            Browser.getWindow().clearTimeout(timeoutId);
        }
        TimeoutHandler dispatchHandler = () -> {
            LOG.debug("Dispatching custom event on view '" + targetView.getLocalName() + "': " + event.getType());
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

    protected void addEventRedirect(Class<? extends Event> eventClass, Element targetview) {
        addEventListener(eventClass, event -> dispatchEventOnView(targetview, event));
    }

    protected void addEventRedirectOnView(Class<? extends Event> eventClass, Element originView, Element destinationView) {
        addEventListenerOnView(originView, eventClass, event -> dispatchEventOnView(destinationView, event));
    }

    protected Document getRootView() {
        return Browser.getWindow().getTop().getDocument();
    }

    protected Element getShellView() {
        return getRootView().querySelector("or-shell");
    }

    protected Element getEditorView() {
        IFrameElement frame = (IFrameElement) getRootView().querySelector("or-shell #editor");
        if (frame == null)
            throw new IllegalArgumentException("Missing or-shell #editor frame");
        Element view = frame.getContentDocument().querySelector("or-editor");
        if (view == null)
            throw new IllegalArgumentException("Missing or-editor view component in editor frame.");
        return view;
    }

    protected Element getConsoleView() {
        IFrameElement frame = (IFrameElement) getRootView().querySelector("or-shell #console");
        if (frame == null)
            throw new IllegalArgumentException("Missing or-shell #console frame");
        Element view = frame.getContentDocument().querySelector("or-console");
        if (view == null)
            throw new IllegalArgumentException("Missing or-console view component in console frame.");
        return view;
    }

}

