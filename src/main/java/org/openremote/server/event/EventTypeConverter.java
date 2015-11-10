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

package org.openremote.server.event;

import org.apache.camel.Converter;
import org.openremote.shared.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.server.util.JsonUtil.JSON;

@Converter
public class EventTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(EventTypeConverter.class);

    @Converter
    public static String writeEvent(Event event) throws Exception {
        LOG.trace("Writing event JSON: " + event);
        return JSON.writeValueAsString(event);
    }

    @Converter
    public static Event readEvent(String string) throws Exception {
        LOG.trace("Reading event JSON: " + string);
        return JSON.readValue(string, Event.class);
    }
}