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
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activate the {@link Resolver} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class ProvisionerActivator implements BundleActivator {

    private ServiceRegistration<Provisioner> registration;

    @Override
    public void start(BundleContext context) throws Exception {

        Bundle bundle = context.getBundle();
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        Module module = runtime.getModule(bundle.getBundleId());
        ModuleContext syscontext = module.getModuleContext();

        ServiceReference<Resolver> resolverRef = syscontext.getServiceReference(Resolver.class);
        Resolver resolver = syscontext.getService(resolverRef);
        ServiceReference<Repository> repositoryRef = syscontext.getServiceReference(Repository.class);
        Repository repository = syscontext.getService(repositoryRef);
        Provisioner provisioner = new DefaultProvisioner(resolver, repository) {

        };
        registration = syscontext.registerService(Provisioner.class, provisioner, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null)
            registration.unregister();
    }
}
