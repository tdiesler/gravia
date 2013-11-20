/*
 * #%L
 * JBossOSGi Runtime
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.jboss.gravia.runtime.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.runtime.Constants;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.NotNullException;

/**
 * The default properties provider.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class DefaultPropertiesProvider implements PropertiesProvider {

    private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
    private final boolean systemPropertyDelegation;

    /**
     * Discover the configuration properties as follows
     * <p>
     * <ol>
     * <li>Use the explicit 'gravia.properties' system property</li>
     * <li>Discover the 'gravia.properties' config file as resource</li>
     * </ol>
     */
    public DefaultPropertiesProvider() {
        this(getDefaultProperties(), true);
    }

    public DefaultPropertiesProvider(Properties props, boolean sysprops) {
        this(propsToMap(props), sysprops);
    }

    public DefaultPropertiesProvider(Map<String, Object> props, boolean sysprops) {
        NotNullException.assertValue(props, "props");
        systemPropertyDelegation = sysprops;
        properties.putAll(props);
    }

    @Override
    public Object getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        Object value = properties.get(key);
        if (value == null && systemPropertyDelegation) {
            value = SecurityActions.getSystemProperty(key, null);
        }
        return value != null ? value : defaultValue;
    }

    private static Properties getDefaultProperties() {
        URL configURL = null;

        // #1 Use the explicit system property
        String sysprop = SecurityActions.getSystemProperty(Constants.GRAVIA_PROPERTIES, null);
        if (sysprop != null) {
            try {
                configURL = new URL(sysprop);
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("Invalid configuration URL: " + sysprop);
            }
        }

        // #2 discover the config file as resource
        if (configURL == null) {
            ClassLoader classLoader = DefaultPropertiesProvider.class.getClassLoader();
            configURL = classLoader.getResource(Constants.GRAVIA_PROPERTIES);
        }

        Properties props = new Properties();
        if (configURL != null) {
            try {
                props.load(configURL.openStream());
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot load configuration from: " + configURL, ex);
            }
        }
        return props;
    }

    private static Map<String, Object> propsToMap(Properties props) {
        Map<String, Object> result = new HashMap<String, Object>();
        synchronized (props) {
            for (Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                result.put(key, value);
            }
        }
        return result;
    }
}
