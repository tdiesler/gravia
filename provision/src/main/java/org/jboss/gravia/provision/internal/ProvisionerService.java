/*
 * #%L
 * Gravia :: Resolver
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.provision.internal;

import java.util.Set;

import javax.management.JMException;

import org.jboss.gravia.provision.DefaultProvisioner;
import org.jboss.gravia.provision.ProvisionException;
import org.jboss.gravia.provision.ProvisionResult;
import org.jboss.gravia.provision.Provisioner;
import org.jboss.gravia.provision.ResourceHandle;
import org.jboss.gravia.provision.ResourceInstaller;
import org.jboss.gravia.provision.spi.RuntimeEnvironment;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resolver.Environment;
import org.jboss.gravia.resolver.Resolver;
import org.jboss.gravia.resource.Requirement;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link Provisioner} component.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-May-2014
 */
@Component(service = { Provisioner.class }, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class ProvisionerService implements Provisioner {

    private Resolver resolver;
    private Repository repository;
    private ResourceInstaller installer;
    private RuntimeEnvironment environment;

    private Provisioner delegate;

    @Activate
    void activate(BundleContext context) throws JMException {
        delegate = new DefaultProvisioner(environment, resolver, repository, installer);
    }

    @Override
    public Environment getEnvironment() {
        return delegate.getEnvironment();
    }

    @Override
    public Resolver getResolver() {
        return delegate.getResolver();
    }

    @Override
    public Repository getRepository() {
        return delegate.getRepository();
    }

    @Override
    public ResourceInstaller getResourceInstaller() {
        return delegate.getResourceInstaller();
    }

    @Override
    public ProvisionResult findResources(Set<Requirement> reqs) {
        return delegate.findResources(reqs);
    }

    @Override
    public Set<ResourceHandle> provisionResources(Set<Requirement> reqs) throws ProvisionException {
        return delegate.provisionResources(reqs);
    }

    @Reference
    void bindResolver(Resolver service) {
        resolver = service;
    }
    void unbindResolver(Resolver service) {
        resolver = null;
    }

    @Reference
    void bindRepository(Repository service) {
        repository = service;
    }
    void unbindRepository(Repository service) {
        repository = null;
    }

    @Reference
    void bindResourceInstaller(ResourceInstaller service) {
        installer = service;
    }
    void unbindResourceInstaller(ResourceInstaller service) {
        installer = null;
    }

    @Reference
    void bindRuntimeEnvironment(RuntimeEnvironment service) {
        environment = service;
    }
    void unbindRuntimeEnvironment(RuntimeEnvironment service) {
        environment = null;
    }
}
