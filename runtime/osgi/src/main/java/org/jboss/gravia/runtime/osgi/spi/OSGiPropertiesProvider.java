/*
 * #%L
 * Gravia :: Runtime :: OSGi
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
package org.jboss.gravia.runtime.osgi.spi;

import org.jboss.gravia.runtime.spi.CompositePropertiesProvider;
import org.jboss.gravia.runtime.spi.EnvPropertiesProvider;
import org.jboss.gravia.runtime.spi.MapPropertiesProvider;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.spi.SubstitutionPropertiesProvider;
import org.jboss.gravia.runtime.spi.SystemPropertiesProvider;
import org.osgi.framework.BundleContext;

/**
 * The default {@link org.jboss.gravia.runtime.spi.PropertiesProvider} for OSGi runtimes.
 */
public class OSGiPropertiesProvider implements PropertiesProvider {

    private final PropertiesProvider delegate;

    public OSGiPropertiesProvider(BundleContext bundleContext) {
        this(bundleContext, true);
    }

    public OSGiPropertiesProvider(BundleContext bundleContext, boolean systemPropertyDelegation) {
        this(bundleContext, systemPropertyDelegation, null);
    }

    public OSGiPropertiesProvider(BundleContext bundleContext, boolean systemPropertyDelegation, String environmentVariablePrefix) {
        this.delegate = new SubstitutionPropertiesProvider(new CompositePropertiesProvider(
                new BundleContextPropertiesProvider(bundleContext),
                systemPropertyDelegation ? new SystemPropertiesProvider() : new MapPropertiesProvider(),
                new EnvPropertiesProvider(environmentVariablePrefix)
        ));
    }

    @Override
    public Object getProperty(String key) {
        return delegate.getProperty(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        return delegate.getProperty(key, defaultValue);
    }
}
