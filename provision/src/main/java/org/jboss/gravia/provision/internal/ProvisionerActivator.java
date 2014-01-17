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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.Constants;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.Module;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
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

        final Bundle bundle = context.getBundle();
        final Runtime runtime = RuntimeLocator.getRequiredRuntime();
        final Module module = runtime.getModule(bundle.getBundleId());
        final ModuleContext syscontext = module.getModuleContext();

        // Add a listener that tracks {@link Resolver} and {@link Repository}
        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<Resolver> resolverRef = new AtomicReference<Resolver>();
        final AtomicReference<Repository> repositoryRef = new AtomicReference<Repository>();
        ServiceListener listener = new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent event) {
                if (event.getType() == ServiceEvent.REGISTERED) {
                    ServiceReference<?> sref = event.getServiceReference();
                    String[] objectClass = (String[]) sref.getProperty(Constants.OBJECTCLASS);
                    if (Resolver.class.getName().equals(objectClass[0])) {
                        resolverRef.set((Resolver) syscontext.getService(sref));
                        latch.countDown();
                    }
                    if (Repository.class.getName().equals(objectClass[0])) {
                        repositoryRef.set((Repository) syscontext.getService(sref));
                        latch.countDown();
                    }
                }
            }
        };
        syscontext.addServiceListener(listener);

        // Double check instead we already missed the event
        ServiceReference<Resolver> resref = syscontext.getServiceReference(Resolver.class);
        if (resref != null) {
            resolverRef.set(syscontext.getService(resref));
            latch.countDown();
        }
        ServiceReference<Repository> repref = syscontext.getServiceReference(Repository.class);
        if (repref != null) {
            repositoryRef.set(syscontext.getService(repref));
            latch.countDown();
        }

        // Await the availability of {@link Resolver} and {@link Repository}
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                String name = resolverRef.get() == null ? Resolver.class.getName() : Repository.class.getName();
                throw new TimeoutException("Cannot obtain " + name + " service");
            }
        } finally {
            syscontext.removeServiceListener(listener);
        }

        // Setup and register the {@link Provisioner} service
        RuntimeEnvironment environment = new RuntimeEnvironment(runtime).initDefaultContent();
        BundleContextResourceInstaller installer = new BundleContextResourceInstaller(context, environment);
        Provisioner provisioner = new DefaultProvisioner(environment, resolverRef.get(), repositoryRef.get(), installer);
        registration = syscontext.registerService(Provisioner.class, provisioner, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (registration != null)
            registration.unregister();
    }
}
