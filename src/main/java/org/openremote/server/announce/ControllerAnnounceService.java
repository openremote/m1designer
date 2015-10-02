package org.openremote.server.announce;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ServletInfo;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.camel.StaticService;
import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.ServletContainerAdapter;
import org.fourthline.cling.transport.spi.StreamServer;
import org.openremote.server.web.UndertowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broadcast our host and port as a UPnP device with some details, but no actual UPnP services.
 */
public class ControllerAnnounceService implements StaticService {

    public static final String SERVICE_CONTEXT_PATH = "/upnp";
    
    private static final Logger LOG = LoggerFactory.getLogger(ControllerAnnounceService.class);

    // Use the already running Undertow server as the UPnP webserver
    class UndertowAdapter implements ServletContainerAdapter {

        @Override
        public void registerServlet(String contextPath, Servlet servlet) {

            ServletInfo servletInfo = Servlets.servlet("UPnPServlet", Servlet.class, () -> new InstanceHandle<Servlet>() {
                @Override
                public Servlet getInstance() {
                    return servlet;
                }

                @Override
                public void release() {
                    // noop
                }
            }).setAsyncSupported(true)
                .setLoadOnStartup(1)
                .addMapping("/*");

            DeploymentInfo deploymentInfo = new DeploymentInfo()
                .addServlet(servletInfo)
                .setContextPath(contextPath)
                .setDeploymentName("UPnPServlet")
                .setClassLoader(ControllerAnnounceService.class.getClassLoader());

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
            manager.deploy();
            try {
                undertowService.getPathHandler().addPrefixPath(contextPath, manager.start());
            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public int addConnector(String host, int port) throws IOException {
            return undertowService.getPort();
        }

        @Override
        public void setExecutorService(ExecutorService executorService) {
            // noop
        }

        @Override
        public void removeConnector(String host, int port) {
            // noop
        }

        @Override
        public void startIfNotRunning() {
            // noop
        }

        @Override
        public void stopIfRunning() {
            // noop
        }
    }

    final protected UndertowService undertowService;

    protected UpnpService upnpService;
    protected JmDNS jmDNSService;

    public ControllerAnnounceService(UndertowService undertowService) {
        this.undertowService = undertowService;

        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                @Override
                public void run() {
                    try {
                        ControllerAnnounceService.this.stop();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        );
    }

    @Override
    public void start() throws Exception {

        startUpnpService();
        startJmDNSService();
    }

    private void startJmDNSService() throws Exception {
        LOG.debug(">>> Starting JmDNS service...");
        URI presentationUri = new URI("http", null, getHostAddress(undertowService.getHost()), undertowService.getPort(), null, null, null);
        jmDNSService = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "OpenRemote Controller", undertowService.getPort(), "");
        Map<String, String> a = new HashMap<>();
        a.put("URL", presentationUri.toURL().toExternalForm());
        a.put("Vendor", "OpenRemote");
        serviceInfo.setText(a);
        jmDNSService.registerService(serviceInfo);
        LOG.debug("<<< JmDNS service started successfully");
    }
    
	private void startUpnpService() throws Exception {
		upnpService = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration() {

            @Override
            protected Namespace createNamespace() {
                return new Namespace(SERVICE_CONTEXT_PATH);
            }

            @Override
            public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
                AsyncServletStreamServerConfigurationImpl configuration =
                    new AsyncServletStreamServerConfigurationImpl(new UndertowAdapter());
                return new AsyncServletStreamServerImpl(configuration) {
                    @Override
                    protected boolean isConnectionOpen(HttpServletRequest request) {
                        // TODO figure out how to get socket in undertow
                        return true;
                    }
                };
            }
        });
        upnpService.getRegistry().addDevice(createDevice());
	}

    @Override
    public void stop() throws Exception {
        if (upnpService != null) {
            upnpService.shutdown();
            upnpService = null;
        }
        
        if (jmDNSService != null) {
        	jmDNSService.unregisterAllServices();
        	jmDNSService = null;
        }
    }

    protected LocalDevice createDevice() throws Exception {

        DeviceIdentity identity =
            new DeviceIdentity(
                UDN.uniqueSystemIdentifier("OpenRemote Controller ")
            );

        DeviceType type = new DeviceType("openremote", "Controller", 1);

        URI presentationUri = new URI("http", null, getHostAddress(undertowService.getHost()), undertowService.getPort(), null, null, null);

        DeviceDetails details =
            new DeviceDetails(
                "OpenRemote Controller on " + getHostAddress(undertowService.getHost()),
                presentationUri
            );

        return new LocalDevice(identity, type, details, new LocalService[0]);
    }

    protected String getHostAddress(String address) throws Exception {
        String hostAddress = address;

        // If we bind to all network addresses/interfaces...
        if (hostAddress.equals("0.0.0.0")) {
            List<NetworkAddress> networkAddresses = upnpService.getRouter().getActiveStreamServers(null);
            if (networkAddresses.size() == 0) {
                throw new RuntimeException("No network addresses found, can't announce controller on: " + hostAddress);
            }
            // Take the first one...
            hostAddress = networkAddresses.get(0).getAddress().getHostAddress();
        }
        return hostAddress;
    }
}
