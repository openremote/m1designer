package org.openremote.beta.server.announce;

import org.apache.camel.StaticService;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.NetworkAddress;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDN;

import java.net.URI;
import java.util.List;

/**
 * Broadcast the webserver's host and port as the presentation URI of an otherwise empty UPnP device.
 */
public class ControllerAnnounceService implements StaticService {

    final protected String presentationHost, presentationPort;

    protected UpnpService upnpService;

    public ControllerAnnounceService(String presentationHost, String presentationPort) {
        this.presentationHost = presentationHost;
        this.presentationPort = presentationPort;

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
        upnpService = new UpnpServiceImpl();

        upnpService.getRegistry().addDevice(
            createDevice(upnpService.getRouter().getActiveStreamServers(null))
        );
    }

    @Override
    public void stop() throws Exception {
        if (upnpService != null) {
            upnpService.shutdown();
            upnpService = null;
        }
    }

    protected LocalDevice createDevice(List<NetworkAddress> networkAddresses) throws Exception {

        String hostAddress = presentationHost;

        // If we bind to all network addresses/interfaces...
        if (hostAddress.equals("0.0.0.0")) {
            if (networkAddresses.size() == 0) {
                throw new RuntimeException("No network addresses found, can't announce controller on: " + hostAddress);
            }
            // Take the first one...
            hostAddress = networkAddresses.get(0).getAddress().getHostAddress();
        }

        DeviceIdentity identity =
            new DeviceIdentity(
                UDN.uniqueSystemIdentifier("OpenRemote Controller ")
            );

        DeviceType type = new DeviceType("openremote", "Controller", 1);

        DeviceDetails details =
            new DeviceDetails(
                "OpenRemote Controller",
                // Use the UPnP presentation URI field to announce our webserver host and port
                new URI("http", null, hostAddress, Integer.valueOf(presentationPort), null, null, null)
            );

        return new LocalDevice(identity, type, details, new LocalService[0]);
    }
}
