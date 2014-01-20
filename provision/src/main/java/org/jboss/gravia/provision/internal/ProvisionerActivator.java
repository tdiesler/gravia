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

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.gravia.Constants;
import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.ModuleActivator;
import org.jboss.gravia.runtime.ModuleContext;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.jboss.gravia.runtime.ServiceEvent;
import org.jboss.gravia.runtime.ServiceListener;
import org.jboss.gravia.runtime.ServiceReference;
import org.jboss.gravia.runtime.ServiceRegistration;

/**
 * Activate the {@link Resolver} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class ProvisionerActivator implements ModuleActivator {

    private AtomicReference<ServiceReference<Resolver>> resolverRef = new AtomicReference<ServiceReference<Resolver>>();
    private AtomicReference<ServiceReference<Repository>> repositoryRef = new AtomicReference<ServiceReference<Repository>>();
    private ServiceRegistration<Provisioner> registration;

    @Override
    public void start(final ModuleContext context) throws Exception {

        // Add a listener that tracks {@link Resolver} and {@link Repository}
        ServiceListener listener = new ServiceListener() {

            @Override
            @SuppressWarnings("unchecked")
            public void serviceChanged(ServiceEvent event) {
                if (event.getType() == ServiceEvent.REGISTERED) {
                    ServiceReference<?> sref = event.getServiceReference();
                    String[] objectClass = (String[]) sref.getProperty(Constants.OBJECTCLASS);
                    if (Resolver.class.getName().equals(objectClass[0])) {
                        resolverRef.compareAndSet(null, (ServiceReference<Resolver>) sref);
                        registerProvisionerService(context, this);
                    } else if (Repository.class.getName().equals(objectClass[0])) {
                        repositoryRef.compareAndSet(null, (ServiceReference<Repository>) sref);
                        registerProvisionerService(context, this);
                    }
                }
            }
        };
        context.addServiceListener(listener);

        // Double check instead we already missed the event
        resolverRef.compareAndSet(null, context.getServiceReference(Resolver.class));
        repositoryRef.compareAndSet(null, context.getServiceReference(Repository.class));

        // Setup and register the {@link Provisioner} service
        registerProvisionerService(context, listener);
    }

    private synchronized void registerProvisionerService(ModuleContext context, ServiceListener listener) {
        ServiceReference<Resolver> resref = resolverRef.get();
        ServiceReference<Repository> repref = repositoryRef.get();
        if (registration == null && resref != null  & repref != null) {
            context.removeServiceListener(listener);
            Resolver resolver = context.getService(resref);
            Repository repository = context.getService(repref);
            Runtime runtime = RuntimeLocator.getRequiredRuntime();
            RuntimeEnvironment environment = new RuntimeEnvironment(runtime).initDefaultContent();
            BundleContextResourceInstaller installer = new BundleContextResourceInstaller(context, environment);
            Provisioner provisioner = new DefaultProvisioner(environment, resolver, repository, installer);
            registration = context.registerService(Provisioner.class, provisioner, null);
        }
    }

    @Override
    public void stop(ModuleContext context) throws Exception {
        if (registration != null) {
            registration.unregister();
        }
        ServiceReference<Resolver> resref = resolverRef.get();
        if (resref != null) {
            context.ungetService(resref);
        }
        ServiceReference<Repository> repref = repositoryRef.get();
        if (repref != null) {
            context.ungetService(repref);
        }
    }
}
