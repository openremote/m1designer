/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.web;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.util.MimeMappings;
import org.apache.camel.StaticService;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.component.servlet.DefaultHttpRegistry;
import org.openremote.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class UndertowService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowService.class);

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
            .addPrefixPath(Constants.REST_SERVICE_CONTEXT_PATH, getCamelServletHandler());

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
            .setContextPath(Constants.REST_SERVICE_CONTEXT_PATH)
            .setDeploymentName("CamelServlet")
            .setClassLoader(WebserverConfiguration.class.getClassLoader());

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy();
        return manager.start();
    }


}
