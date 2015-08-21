package org.openremote.beta.client.shell;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import elemental.dom.Element;
import elemental.html.IFrameElement;
import org.openremote.beta.client.console.ConsoleRefreshEvent;
import org.openremote.beta.client.editor.EditorOpenedEvent;
import org.openremote.beta.client.editor.flow.crud.FlowDeletedEvent;
import org.openremote.beta.client.editor.flow.editor.FlowEditEvent;
import org.openremote.beta.client.editor.flow.editor.FlowUpdatedEvent;
import org.openremote.beta.client.shared.session.message.MessageReceivedEvent;
import org.openremote.beta.client.shared.session.message.MessageSessionPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class ShellPresenter extends MessageSessionPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ShellPresenter.class);

    public ShellPresenter(com.google.gwt.dom.client.Element view) {
        super(view);

        addEventListener(
            MessageReceivedEvent.class,
            event -> {
                if (isEditorViewAvailable()) {
                    dispatchEvent(getEditorView(), event);
                    dispatchEvent("#messageLog", event);
                }
                dispatchEvent(getConsoleView(), event);
            }
        );

        addEventListener(EditorOpenedEvent.class, event -> dispatchEvent(getConsoleView(), event));
        addEventListener(EditorClosedEvent.class, event -> dispatchEvent(getConsoleView(), event));

        addEventListener(
            FlowEditEvent.class,
            event -> {
                dispatchEvent("#messageLog", event);
                dispatchEvent(getConsoleView(), new ConsoleRefreshEvent(event.getFlow()));
            }
        );

        addEventListener(
            FlowUpdatedEvent.class,
            event -> dispatchEvent(getConsoleView(), new ConsoleRefreshEvent(event.getFlow()))
        );

        addEventListener(
            FlowDeletedEvent.class,
            event -> dispatchEvent(getConsoleView(), new ConsoleRefreshEvent(null))
        );
    }

    protected boolean isEditorViewAvailable() {
        IFrameElement frame = (IFrameElement) getRequiredChildView("#editor");
        Element view = frame.getContentDocument().querySelector("or-editor");
        return view != null;
    }

    protected Element getEditorView() {
        IFrameElement frame = (IFrameElement) getRequiredChildView("#editor");
        Element view = frame.getContentDocument().querySelector("or-editor");
        if (view == null)
            throw new IllegalArgumentException("Missing or-editor view component in editor frame.");
        return view;
    }

    protected Element getConsoleView() {
        IFrameElement frame = (IFrameElement) getRequiredChildView("#console");
        Element view = frame.getContentDocument().querySelector("or-console");
        if (view == null)
            throw new IllegalArgumentException("Missing or-console view component in console frame.");
        return view;
    }


}
