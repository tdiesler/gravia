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

/**
 * A Composite {@link org.jboss.gravia.runtime.spi.PropertiesProvider}.
 */
public class CompositePropertiesProvider implements PropertiesProvider {

    private final PropertiesProvider[] delegates;

    public CompositePropertiesProvider(PropertiesProvider... delegates) {
        this.delegates = delegates;
    }

    @Override
    public Object getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        Object result = null;
        for (PropertiesProvider delegate : delegates) {
            result = delegate.getProperty(key);
            if (result != null) {
                return result;
            }
        }
        return defaultValue;
    }
}
