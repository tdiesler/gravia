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
package org.jboss.gravia.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


/**
 * [TODO]
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 */
public final class DefaultPropertiesProvider implements PropertiesProvider {

    private final Map<String, Object> properties;

    public DefaultPropertiesProvider() {
        this(System.getProperties());
    }

    @SuppressWarnings("unchecked")
    public DefaultPropertiesProvider(Map<String, Object> props) {
        properties = new ConcurrentHashMap<String, Object>(props != null ? props : Collections.EMPTY_MAP);
    }

    public DefaultPropertiesProvider(Properties props) {
        properties = new ConcurrentHashMap<String, Object>(toMap(props));
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

    private Map<String, Object> toMap(Properties props) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        if (props != null) {
            for (Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                result.put(key, value);
            }
        }
        return result;
    }
}
