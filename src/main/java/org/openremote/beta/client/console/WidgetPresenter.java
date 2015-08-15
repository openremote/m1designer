package org.openremote.beta.client.console;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsType;
import org.openremote.beta.client.shared.AbstractPresenter;
import org.openremote.beta.shared.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsExport
@JsType
public class WidgetPresenter extends AbstractPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetPresenter.class);

    protected final Widget widget;

    public WidgetPresenter(com.google.gwt.dom.client.Element gwtView, Widget widget) {
        super(gwtView);
        this.widget = widget;
    }

}
