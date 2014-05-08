package org.jboss.gravia.provision.internal;

/*
 * #%L
 * Gravia :: Provision
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

import java.util.HashSet;
import java.util.Set;

import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.RuntimeLocator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activate the {@link Resolver} in the runtime.
 *
 * @author thomas.diesler@jboss.com
 * @since 20-Dec-2012
 */
public final class ProvisionerActivator implements BundleActivator {

    private Set<ServiceRegistration<?>> registrations = new HashSet<ServiceRegistration<?>>();

    @Override
    public void start(final BundleContext context) throws Exception {
        Runtime runtime = RuntimeLocator.getRequiredRuntime();
        RuntimeEnvironment environment = new RuntimeEnvironment(runtime).initDefaultContent();
        BundleResourceInstaller installer = new BundleResourceInstaller(context, environment);
        registrations.add(context.registerService(RuntimeEnvironment.class, environment, null));
        registrations.add(context.registerService(ResourceInstaller.class, installer, null));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration<?> sreg : registrations) {
            sreg.unregister();
        }
    }
}
