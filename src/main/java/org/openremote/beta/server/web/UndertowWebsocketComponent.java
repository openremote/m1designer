package org.openremote.beta.server.web;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import io.undertow.websockets.jsr.DefaultContainerConfigurator;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.openremote.beta.server.web.socket.WebsocketAdapter;
import org.openremote.beta.server.web.socket.WebsocketCORSFilter;
import org.openremote.beta.server.web.socket.WebsocketComponent;
import org.openremote.beta.server.web.socket.WebsocketConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Map;

public class UndertowWebsocketComponent extends WebsocketComponent {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowWebsocketComponent.class);

    public static final String SERVICE_CONTEXT_PATH = "/ws";

    final protected ServletContainer servletContainer = Servlets.defaultContainer();
    protected DeploymentInfo deploymentInfo;
    protected DeploymentManager deploymentManager;

    protected UndertowService getUndertowService() {
        UndertowService undertowService = getCamelContext().hasService(UndertowService.class);
        if (undertowService == null)
            throw new IllegalStateException("Please configure and add " + UndertowService.class.getName());
        return undertowService;
    }

    @Override
    protected void deploy() throws Exception {
        LOG.info("Deploying websocket endpoints: " + getConsumers().keySet());

        WebSocketDeploymentInfo websocketDeploymentInfo = new WebSocketDeploymentInfo();

        for (Map.Entry<String, WebsocketConsumer> entry : getConsumers().entrySet()) {
            websocketDeploymentInfo.addEndpoint(
                ServerEndpointConfig.Builder.create(WebsocketAdapter.class, "/" + entry.getKey())
                    .configurator(new DefaultContainerConfigurator() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                            return (T) new WebsocketAdapter(entry.getValue());
                        }
                    })
                    .build()
            );
        }

        deploymentInfo = new DeploymentInfo()
            .setDeploymentName("WebSocket Deployment")
            .setContextPath(SERVICE_CONTEXT_PATH)
            .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, websocketDeploymentInfo)
            .setClassLoader(WebsocketComponent.class.getClassLoader());

        // TODO Per-endpoint CORS filter
        if (getAllowedOrigin() != null) {
            WebsocketCORSFilter websocketCORSFilter = new WebsocketCORSFilter();
            FilterInfo filterInfo = new FilterInfo("WebSocket CORS Filter", WebsocketCORSFilter.class, () -> new InstanceHandle<Filter>() {
                @Override
                public Filter getInstance() {
                    return websocketCORSFilter;
                }

                @Override
                public void release() {
                }
            }).addInitParam(WebsocketCORSFilter.ALLOWED_ORIGIN, getAllowedOrigin());
            deploymentInfo.addFilter(filterInfo);
            deploymentInfo.addFilterUrlMapping(filterInfo.getName(), "/*", DispatcherType.REQUEST);
        }

        deploymentManager = servletContainer.addDeployment(deploymentInfo);
        deploymentManager.deploy();

        HttpHandler handler = deploymentManager.start();

        getUndertowService().getPathHandler().addPrefixPath(SERVICE_CONTEXT_PATH, handler);
    }

    @Override
    protected void undeploy() throws Exception {
        if (deploymentManager != null) {
            deploymentManager.stop();
            deploymentManager.undeploy();
            servletContainer.removeDeployment(deploymentInfo);
            getUndertowService().getPathHandler().removePrefixPath(SERVICE_CONTEXT_PATH);

            deploymentInfo = null;
            deploymentManager = null;
        }
    }
}
