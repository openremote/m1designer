package org.openremote.client.shell.help;

import jsinterop.annotations.JsType;
import org.openremote.client.shared.AbstractPresenter;
import org.openremote.client.shared.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsType
public class HelpPresenter extends AbstractPresenter<View> {

    private static final Logger LOG = LoggerFactory.getLogger(HelpPresenter.class);

    public HelpPresenter(View view) {
        super(view);
    }
}
