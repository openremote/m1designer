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

package org.openremote.client.shared;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface View {

    @JsProperty
    String getLocalName();

    Object $$(String selector);

    Object fire(String type, Object detail);

    Object get(String path);

    Object get(String[] paths);

    Object get(String path, Object root);

    Object get(String[] paths, Object root);

    void set(String path, Object value);

    void set(String path, Object value, Object root);

    void set(String path, boolean value);

    void set(String path, boolean value, Object root);

    void toggleAttribute(String name, boolean b, Object node);

    void toggleClass(String name, boolean b, Object node);

}
