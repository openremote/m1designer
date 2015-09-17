package org.openremote.beta.server.web;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowLogger;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.*;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.util.MimeMappings;
import org.apache.camel.StaticService;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.component.servlet.DefaultHttpRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.*;

public class UndertowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowService.class);

    public static final String SERVICE_CONTEXT_PATH = "/svc";

    final protected boolean devMode;
    final protected String host;
    final protected int port;
    final protected String documentRoot;
    final protected int documentCacheSeconds;

    protected Undertow server;
    protected PathHandler pathHandler;

    public UndertowService(boolean devMode, String host, int port, String documentRoot, int documentCacheSeconds) {
        this.devMode = devMode;
        this.host = host;
        this.port = port;
        this.documentRoot = documentRoot;
        this.documentCacheSeconds = documentCacheSeconds;
    }

    public Undertow getServer() {
        return server;
    }

    public PathHandler getPathHandler() {
        return pathHandler;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void start() throws Exception {
        pathHandler = Handlers.path()
            .addPrefixPath("/", getStaticResourceHandler())
            .addPrefixPath(SERVICE_CONTEXT_PATH, getCamelServletHandler());

        server = Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(new UndertowErrorHandler(devMode, pathHandler))
            .build();

        // TODO: XNIO worker and buffer pool should be configurable...

        server.start();
        LOG.info("HTTP/websocket server ready on: " + host + ":" + port);
    }

    @Override
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
            pathHandler = null;
            DefaultHttpRegistry.removeHttpRegistry("CamelServlet"); // WTF, static!?
        }
    }

    protected HttpHandler getStaticResourceHandler() throws Exception {
        File documentRootFile = new File(documentRoot);
        LOG.info("Configuring static resource handler for filesystem path " + documentRootFile.getAbsolutePath());
        ResourceManager staticResourcesManager = new JarFileResourceManager(documentRootFile, 0, true, false);

        MimeMappings.Builder mimeBuilder = MimeMappings.builder(true);
        mimeBuilder.addMapping("wsdl", "application/xml");
        mimeBuilder.addMapping("xsl", "text/xsl");
        // TODO: Add more mime/magic stuff?

        return new ResourceHandler(staticResourcesManager)
            .setCachable(value -> !(value.getRequestPath().contains("nocache") || devMode))
            .setCacheTime(documentCacheSeconds)
            .setDirectoryListingEnabled(false)
            .setMimeMappings(mimeBuilder.build());
    }

    protected HttpHandler getCamelServletHandler() throws Exception {
        ServletInfo camelServlet = Servlets.servlet("CamelServlet", CamelHttpTransportServlet.class)
            .setAsyncSupported(true)
            .setLoadOnStartup(1)
            .addMapping("/*");

        DeploymentInfo deploymentInfo = new DeploymentInfo()
            .addServlet(camelServlet)
            .setContextPath(SERVICE_CONTEXT_PATH)
            .setDeploymentName("CamelServlet")
            .setClassLoader(WebserverConfiguration.class.getClassLoader());

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        return manager.start();
    }


}
