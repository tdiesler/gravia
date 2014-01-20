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

import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration;
import org.jboss.gravia.repository.RepositoryRuntimeRegistration.Registration;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.util.RuntimePropertiesProvider;

/**
 * Activate the {@link Repository} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class RepositoryActivator implements ModuleActivator {

    private Registration registration;

    @Override
    public void start(ModuleContext context) throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        DefaultRepository repository = new DefaultRepository(new RuntimePropertiesProvider(runtime));
        registration =  RepositoryRuntimeRegistration.registerRepository(context, repository);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
    }
}
