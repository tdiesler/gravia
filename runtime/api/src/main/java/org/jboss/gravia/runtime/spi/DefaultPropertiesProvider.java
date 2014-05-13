/*
 * #%L
 * Gravia :: Runtime :: API
 * %%
 * Copyright (C) 2013 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.jboss.gravia.runtime.spi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.gravia.utils.ArgumentAssertion;

/**
 * The default properties provider.
 *
 * Discover the configuration properties as follows
 * <p>
 * <ol>
 * <li>Use the explicit 'gravia.properties' system property</li>
 * <li>Discover the 'gravia.properties' config file as resource</li>
 * </ol>
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Sep-2013
 *
 * @ThreadSafe
 */
public class DefaultPropertiesProvider implements PropertiesProvider {

    private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
    private final boolean systemPropertyDelegation;

    public DefaultPropertiesProvider() {
        this(new HashMap<String, Object>(), true);
    }

    public DefaultPropertiesProvider(Properties props, boolean sysprops) {
        this(propsToMap(props), sysprops);
    }

    public DefaultPropertiesProvider(Map<String, Object> props, boolean sysprops) {
        ArgumentAssertion.assertNotNull(props, "props");
        systemPropertyDelegation = sysprops;
        properties.putAll(propsToMap(getDefaultProperties()));
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
        String sysprop = SecurityActions.getSystemProperty(org.jboss.gravia.Constants.GRAVIA_PROPERTIES, null);
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
            configURL = classLoader.getResource(org.jboss.gravia.Constants.GRAVIA_PROPERTIES);
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
