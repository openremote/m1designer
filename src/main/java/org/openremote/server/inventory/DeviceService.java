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

package org.openremote.server.inventory;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.StaticService;
import org.openremote.server.persistence.PersistenceService;
import org.openremote.server.persistence.inventory.DeviceDAO;
import org.openremote.server.util.IdentifierUtil;
import org.openremote.shared.inventory.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class DeviceService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceService.class);

    protected final CamelContext context;

    final protected List<Device> devices = new ArrayList<>();

    public DeviceService(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    public Device[] getDevices(@Header("onlyReady") Boolean onlyReady) {
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            DeviceDAO dao = ps.getDAO(em, DeviceDAO.class);
            List<Device> devices = dao.findAll(onlyReady);
            return devices.toArray(new Device[devices.size()]);
        });
    }

    public Device getDevice(@Header("id") String id) {
        LOG.debug("Get device: " + id);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            DeviceDAO dao = ps.getDAO(em, DeviceDAO.class);
            return dao.findById(id);
        });
    }

    public String postDevice(Device device) {
        LOG.debug("Posting device: " + device);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            DeviceDAO dao = ps.getDAO(em, DeviceDAO.class);

            if (device.getId() == null) {
                device.setId(IdentifierUtil.generateGlobalUniqueId());
            }

            return dao.makePersistent(device).getId();
        });
    }

    public boolean putDevice(String id, Device device) {
        LOG.debug("Putting device: " + id);
        return context.hasService(PersistenceService.class).transactional((ps, em) -> {
            DeviceDAO dao = ps.getDAO(em, DeviceDAO.class);
            Device existing = dao.findById(id);
            if (existing == null)
                return false;
            dao.makePersistent(device);
            return true;
        });
    }

    public void deleteDevice(@Header("id") String id) {
        LOG.debug("Deleting device: " + id);
        context.hasService(PersistenceService.class).transactional((ps, em) -> {
            DeviceDAO dao = ps.getDAO(em, DeviceDAO.class);
            Device existing = dao.findById(id);
            if (existing != null)
                dao.makeTransient(existing);
            return null;
        });
    }

    public void setDeviceOffline(String id) {
        Device device = getDevice(id);
        if (device != null) {
            device.setStatus(Device.Status.OFFLINE);
            putDevice(id, device);
        }
    }

    synchronized public void addDevices(Device[] devices) {
        Device[] existingDevices = getDevices(false);
        for (Device device : devices) {
            boolean exists = false;
            boolean existsUninitialized = false;
            for (Device existingDevice : existingDevices) {
                if (existingDevice.getId().equals(device.getId())) {
                    exists = true;
                    existsUninitialized = existingDevice.getStatus().equals(Device.Status.UNINITIALIZED);
                    break;
                }
            }

            if (!exists) {
                postDevice(device);
            } else if (existsUninitialized) {
                deleteDevice(device.getId());
                postDevice(device);
            }
        }
    }

}
