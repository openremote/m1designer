package org.openremote.client.shared;

import com.google.gwt.core.client.JavaScriptObject;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.fusesource.restygwt.client.Defaults;
import org.openremote.client.event.ConfirmationEvent;
import org.openremote.shared.event.Event;
import org.openremote.shared.event.bus.EventBus;
import org.openremote.shared.event.bus.EventListener;
import org.openremote.shared.event.bus.EventRegistration;
import org.openremote.shared.func.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JsType
public abstract class AbstractPresenter<V extends View> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPresenter.class);

    static {
        // Use unix timestamps when handling JSON and java.util.Date
        Defaults.setDateFormat(null);
    }

    final protected V view;
    final protected List<EventRegistration> eventRegistrations = new ArrayList<>();

    public boolean dirty;

    public AbstractPresenter(V view) {
        this.view = view;
        if (view == null)
            throw new IllegalArgumentException("Can't instantiate presenter without a view element: " + getClass().getName());
        LOG.debug("Creating presenter for view '" + view.getLocalName() + "': " + getClass().getName());
    }

    public V getView() {
        return view;
    }

    public View getViewChildComponent(String selector) {
        return (View) getRequiredElement(selector);
    }

    public DOM getViewRootDOM() {
        return getDOMRoot(getView());
    }

    public DOM getDOM(View view) {
        return JsUtil.dom(view);
    }

    public DOM getDOMRoot(View view) {
        return JsUtil.domRoot(view);
    }

    public void ready() {
        LOG.debug("Ready: " + getView().getLocalName());
    }

    public void attached() {
        LOG.debug("Attached: " + getView().getLocalName());
        EventBus.addAll(eventRegistrations);
    }

    public void detached() {
        LOG.debug("Detached: " + getView().getLocalName());
        EventBus.removeAll(eventRegistrations);
    }

    protected native boolean notifyPath(String path, String s) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, s);
    }-*/;

    protected native boolean notifyPath(String path, double d) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, d);
    }-*/;

    protected native boolean notifyPath(String path, int i) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, i);
    }-*/;

    protected native boolean notifyPath(String path, boolean b) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, b);
    }-*/;

    protected native boolean notifyPath(String path, Object[] array) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, array);
    }-*/;

    protected native boolean notifyPath(String path, JavaScriptObject jso) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, jso);
    }-*/;

    protected native boolean notifyPathNull(String path) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, null);
    }-*/;

    // TODO: This is dangerous, if the path is a JS Object you must access one of its keys to get the real value!
    protected native boolean notifyPath(String path) /*-{
        return this.@AbstractPresenter::view.notifyPath("_presenter." + path, Math.random()); // Always trigger an update inside Polymer!
    }-*/;

    protected native void pushArray(String array, Object obj) /*-{
        this.@AbstractPresenter::view.push("_presenter." + array, obj);
    }-*/;

    protected <E extends Event> EventRegistration<E> addPrepareListener(Class<E> eventClass,
                                                                        EventListener<E> listener) {
        return addListener(true, eventClass, listener);
    }

    protected <E extends Event> EventRegistration<E> addPrepareListener(EventListener<E> listener) {
        return addListener(true, null, listener);
    }

    protected <E extends Event> EventRegistration<E> addListener(Class<E> eventClass,
                                                 EventListener<E> listener) {
        return addListener(false, eventClass, listener);
    }

    protected <E extends Event> EventRegistration<E> addListener(EventListener<E> listener) {
        return addListener(false, null, listener);
    }

    @JsIgnore
    @SuppressWarnings("unchecked")
    public <E extends Event> EventRegistration<E> addListener(boolean prepare,
                                              Class<E> eventClass,
                                              EventListener<E> listener) {
        EventRegistration<E> registration;
        if (eventClass != null) {
            String eventType = Event.getType(eventClass);
            if (prepare) {
                LOG.debug("Adding event prepare listener: " + eventType);
            } else {
                LOG.debug("Adding event listener: " + eventType);
            }
            registration = new EventRegistration(prepare, eventClass, listener);
        } else {
            LOG.debug("Adding catchall event listener: " + listener.getClass().getName());
            registration = new EventRegistration(prepare, listener);
        }
        eventRegistrations.add(registration);
        return registration;
    }

    protected void dispatch(Event event, int debounceMillis, EventRegistration... skipRegistrations) {
        Timeout.debounce(event.getType(), () -> {
            dispatch(event, skipRegistrations);
        }, debounceMillis);
    }

    @SuppressWarnings("unchecked")
    protected void dispatch(Event event, EventRegistration... skipRegistrations) {
        LOG.debug("Dispatching event: " + event.getType());
        EventBus.dispatch(event, skipRegistrations);
    }

    protected boolean hasViewElement(String selector) {
        return getOptionalElement(selector) != null;
    }

    protected Object getOptionalElement(String selector) {
        return getView().$$(selector);
    }

    protected Object getRequiredElement(String selector) {
        Object child = getOptionalElement(selector);
        if (child == null)
            throw new RuntimeException("Missing child element '" + selector + "' on: " + getView().getLocalName());
        return child;
    }

    protected DOM getRequiredElementDOM(String selector) {
        return getDOM((View)getRequiredElement(selector));
    }

    protected String getWindowQueryArgument(String parameter) {
        return JsUtil.getQueryArgument(parameter);
    }

    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
        notifyPath("dirty", dirty);
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void confirmIfDirty(Callback confirmAction) {
        confirmIfDirty(confirmAction, null);
    }

    protected void confirmIfDirty(Callback confirmAction, Callback cancelAction) {
        if (dirty) {
            dispatchDirtyConfirmation(() -> {
                setDirty(false);
                confirmAction.call();
            }, cancelAction);
        } else {
            confirmAction.call();
        }
    }

    protected void dispatchDirtyConfirmation(Callback confirmAction, Callback cancelAction) {
        dispatch(new ConfirmationEvent(
            "Unsaved Changes",
            "You have not saved your modifications. Continue without saving?",
            confirmAction,
            cancelAction
        ));
    }

}

