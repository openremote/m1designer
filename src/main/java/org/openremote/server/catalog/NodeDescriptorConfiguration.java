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

package org.openremote.server.catalog;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class NodeDescriptorConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDescriptorConfiguration.class);

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        Iterable<NodeDescriptor> discoveredNodeDescriptors =
            ServiceLoader.load(NodeDescriptor.class);

        for (NodeDescriptor descriptor : discoveredNodeDescriptors) {
            LOG.info("Discovered node descriptor: " + descriptor.getType());
            if (environment.getRegistry().containsKey(descriptor.getType())) {
                throw new IllegalStateException("Duplicate node descriptor type: " + descriptor.getType());
            }
            environment.getRegistry().put(descriptor.getType(), descriptor);
        }
    }

}
