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

package org.openremote.server;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;

import java.util.Properties;

/**
 * Wrap the Camel property support.
 *
 * The Camel guys seem to be busy dealing with the Springsanity and Blueballsprint configuration nightmare.
 * This class makes Camel properties component usable for simply looking up environment variables or falling
 * back to programmatic overrides in unit tests.
 */
public class Environment {

    public static final String DEV_MODE = "DEV_MODE";
    public static final String DEV_MODE_DEFAULT = "true";

    final protected CamelContext context;
    final protected Properties overrides;
    final protected boolean useEnvironmentVariables;

    public Environment(CamelContext context) {
        this(context, true, null);
    }

    public Environment(CamelContext context, boolean useEnvironmentVariables, Properties overrides) {
        this.context = context;
        this.useEnvironmentVariables = useEnvironmentVariables;
        this.overrides = overrides;
        configurePropertiesComponent();
    }

    public String getProperty(String property) {
        return getProperty(property, null);
    }

    public String getProperty(String property, String defaultValue) {
        String placeholder = createPlaceholder(property, defaultValue);
        try {
            return context.resolvePropertyPlaceholders(placeholder);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Value of environment setting '" + property + "' not found, and no default value set."
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public SimpleRegistry getRegistry() {
        return (SimpleRegistry) ((PropertyPlaceholderDelegateRegistry)context.getRegistry()).getRegistry();
    }

    protected String createPlaceholder(String property, String defaultValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("{{");
        if (useEnvironmentVariables)
            sb.append("env:");
        sb.append(property);
        if (defaultValue != null)
            sb.append(":").append(defaultValue);
        sb.append("}}");
        return sb.toString();
    }

    protected void configurePropertiesComponent() {
        PropertiesComponent pc = new PropertiesComponent() {
            @Override
            public boolean isDefaultCreated() {
                return false; // We explicitly configure it here
            }
        };
        if (overrides != null && !overrides.isEmpty()) {
            pc.setOverrideProperties(overrides);
        }
        context.addComponent("properties", pc);
    }

}
