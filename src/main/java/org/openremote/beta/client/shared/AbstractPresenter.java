package org.openremote.beta.client.shared;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.dom.Node;
import org.openremote.beta.client.event.ConfirmationEvent;
import org.openremote.beta.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JsExport
@JsType
public abstract class AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPresenter.class);

    final protected Element view;
    final protected List<EventRegistration> eventRegistrations = new ArrayList<>();

    public boolean dirty;

    public AbstractPresenter(com.google.gwt.dom.client.Element gwtView) {
        this.view = gwtView.cast(); // TODO No idea why this is necessary, without this we can only use superdevmode...

        if (view == null)
            throw new IllegalArgumentException("Can't instantiate presenter without a view element: " + getClass().getName());
        LOG.debug("Creating presenter for view '" + view.getLocalName() + "': " + getClass().getName());
    }

    public Element getView() {
        return view;
    }

    public Component getViewComponent() {
        return (Component) view;
    }

    public Component getViewChildComponent(String selector) {
        return (Component) getRequiredElement(selector);
    }

    public Component.DOM getViewRootDOM() {
        return getDOMRoot(getView());
    }

    public Component.DOM getDOM(Node node) {
        return JsUtil.dom(node);
    }

    public Component.DOM getDOMRoot(Element element) {
        return JsUtil.domRoot(element);
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

    protected <E extends Event> void addPrepareListener(Class<E> eventClass,
                                                        EventListener<E> listener) {
        addListener(true, eventClass, listener);
    }

    protected <E extends Event> void addListener(Class<E> eventClass,
                                                 EventListener<E> listener) {
        addListener(false, eventClass, listener);
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void addListener(boolean prepare,
                                              Class<E> eventClass,
                                              EventListener<E> listener) {
        String eventType = Event.getType(eventClass);
        if (prepare) {
            LOG.debug("Adding event prepare listener: " + eventType);
        } else {
            LOG.debug("Adding event listener: " + eventType);
        }
        eventRegistrations.add(new EventRegistration(prepare, eventClass, listener));
    }

    protected void dispatch(Event event, int debounceMillis) {
        Timeout.debounce(event.getType(), () -> {
            dispatch(event);
        }, debounceMillis);
    }

    @SuppressWarnings("unchecked")
    protected void dispatch(Event event) {
        EventBus.dispatch(event);
    }

    protected boolean hasViewElement(String selector) {
        return getOptionalElement(selector) != null;
    }

    protected Element getOptionalElement(String selector) {
        return getViewComponent().$$(selector);
    }

    protected Element getRequiredElement(String selector) {
        Element child = getOptionalElement(selector);
        if (child == null)
            throw new RuntimeException("Missing child element '" + selector + "' on: " + getView().getLocalName());
        return child;
    }

    protected Component.DOM getRequiredElementDOM(String selector) {
        return getDOM(getRequiredElement(selector));
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

