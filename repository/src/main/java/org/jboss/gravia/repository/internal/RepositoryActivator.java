package org.jboss.gravia.repository.internal;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2012 - 2013 JBoss by Red Hat
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

import java.io.File;

import org.jboss.gravia.Constants;
import org.jboss.gravia.repository.DefaultMavenDelegateRepository;
import org.jboss.gravia.repository.DefaultRepositoryStorage;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryBuilder;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration.Registration;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.runtime.util.RuntimePropertiesProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class RepositoryActivator implements BundleActivator {

    private Registration registration;

    @Override
    public void start(final BundleContext context) throws Exception {

        // Create the {@link PropertiesProvider}
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        PropertiesProvider propertyProvider = new RuntimePropertiesProvider(runtime) {
            @Override
            public Object getProperty(String key, Object defaultValue) {
                Object value = super.getProperty(key, defaultValue);
                if (value == null && Constants.PROPERTY_REPOSITORY_STORAGE_DIR.equals(key)) {
                    File dirname = context.getBundle().getDataFile("repository");
                    value = dirname.getAbsolutePath();
                }
                return value != null ? (String) value : null;
            }
        };

        RepositoryBuilder builder = new RepositoryBuilder(propertyProvider);
        builder.setRepositoryDelegate(new DefaultMavenDelegateRepository(propertyProvider));
        builder.setRepositoryStorage(new DefaultRepositoryStorage(propertyProvider));
        registration =  RepositoryRuntimeRegistration.registerRepository(runtime, builder.getRepository());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
    }
}
