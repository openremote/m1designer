package org.openremote.beta.server.web.socket;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.ObjectHelper;

import java.util.Map;

@UriEndpoint(scheme = "ws", title = "Undertow Websocket", syntax = "ws:resourceUri", consumerClass = WebsocketConsumer.class, label = "http,websocket")
public class WebsocketEndpoint extends DefaultEndpoint {

    private WebsocketComponent component;

    @UriPath
    @Metadata(required = "true")
    private String resourceUri;

    @UriParam
    private Boolean sendToAll;

    public WebsocketEndpoint(WebsocketComponent component, String uri, String resourceUri, Map<String, Object> parameters) {
        super(uri, component);
        this.resourceUri = resourceUri;
        this.component = component;
    }

    @Override
    public WebsocketComponent getComponent() {
        ObjectHelper.notNull(component, "component");
        return (WebsocketComponent) super.getComponent();
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        ObjectHelper.notNull(component, "component");
        WebsocketConsumer consumer = new WebsocketConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new WebsocketProducer(this);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public WebsocketSessions getWebsocketSessions() {
        return getComponent().getWebsocketSessions();
    }

    public void connect(WebsocketConsumer consumer) throws Exception {
        component.connect(consumer);
    }

    public void disconnect(WebsocketConsumer consumer) throws Exception {
        component.disconnect(consumer);
    }

    public Boolean getSendToAll() {
        return sendToAll;
    }

    public void setSendToAll(Boolean sendToAll) {
        this.sendToAll = sendToAll;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }
}
