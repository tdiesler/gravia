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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * A properties provider that delegates to the system properties
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public final class DefaultPropertiesProvider implements PropertiesProvider {

    private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();

    public DefaultPropertiesProvider() {
        this(System.getProperties());
    }

    public DefaultPropertiesProvider(Map<String, Object> props) {
        if (props != null)
            properties.putAll(props);
    }

    public DefaultPropertiesProvider(Properties props) {
        if (props != null) {
            for (Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                properties.put(key, value);
            }
        }
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        Object value = properties.get(key);
        return value != null ? value : defaultValue;
    }
}
