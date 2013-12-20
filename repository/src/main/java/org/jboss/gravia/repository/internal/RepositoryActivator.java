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

import org.jboss.gravia.repository.DefaultMavenIdentityRepository;
import org.jboss.gravia.repository.DefaultPersistentRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryAggregator;
import org.jboss.gravia.repository.Repository.ConfigurationPropertyProvider;
import org.jboss.gravia.runtime.DefaultBundleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.ServiceRegistration;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class RepositoryActivator extends DefaultBundleActivator {

    private ServiceRegistration<Repository> registration;

    @Override
    public void start(final ModuleContext context) throws Exception {

        // Create the {@link ConfigurationPropertyProvider}
        final ConfigurationPropertyProvider propertyProvider = new ConfigurationPropertyProvider() {
            @Override
            public String getProperty(String key, String defaultValue) {
                Runtime runtime = context.getModule().adapt(Runtime.class);
                Object value = runtime.getProperty(key, defaultValue);
                if (value == null && Repository.PROPERTY_REPOSITORY_STORAGE_DIR.equals(key)) {
                    File dirname = context.getModule().getDataFile("repository");
                    value = dirname.getAbsolutePath();
                }
                return value != null ? (String) value : null;
            }
        };

        DefaultMavenIdentityRepository mavenRepo = new DefaultMavenIdentityRepository(propertyProvider);
        Repository repository = new DefaultPersistentRepository(propertyProvider, new RepositoryAggregator(mavenRepo));
        registration = context.registerService(Repository.class, repository, null);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
    }
}
