package org.jboss.gravia.provision.internal;

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

import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.DefaultBundleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;

/**
 * Activate the {@link Resolver} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class ProvisionerActivator extends DefaultBundleActivator {

    private ServiceReference<Resolver> resolverRef;
    private ServiceReference<Repository> repositoryRef;
    private ServiceRegistration<Provisioner> registration;

    @Override
    public void start(final ModuleContext context) throws Exception {
        resolverRef = context.getServiceReference(Resolver.class);
        Resolver resolver = context.getService(resolverRef);
        repositoryRef = context.getServiceReference(Repository.class);
        Repository repository = context.getService(repositoryRef);
        Provisioner provisioner = new DefaultProvisioner(resolver, repository) {

        };
        registration = context.registerService(Provisioner.class, provisioner, null);
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (registration != null)
            registration.unregister();
        if (repositoryRef != null)
            context.ungetService(repositoryRef);
        if (resolverRef != null)
            context.ungetService(resolverRef);
    }
}
