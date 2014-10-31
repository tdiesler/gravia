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
package org.jboss.gravia.repository.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.management.JMException;

import org.jboss.gravia.repository.DefaultRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.Runtime;
import org.jboss.gravia.runtime.spi.RuntimePropertiesProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link Repository} component.
 *
 * @author thomas.diesler@jboss.com
 * @since 08-May-2014
 */
@Component(service = { Repository.class }, configurationPolicy = ConfigurationPolicy.IGNORE, immediate = true)
public final class RepositoryService implements Repository {

    private Repository delegate;
    private Runtime runtime;

    @Activate
    void activate(BundleContext context) throws JMException {
        delegate = new DefaultRepository(new RuntimePropertiesProvider(runtime));
    }

    @Override
    public <T> T adapt(Class<T> type) {
        return delegate.adapt(type);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Collection<Capability> findProviders(Requirement requirement) {
        return delegate.findProviders(requirement);
    }

    @Override
    public Map<Requirement, Collection<Capability>> findProviders(Collection<Requirement> requirements) {
        return delegate.findProviders(requirements);
    }

    @Override
    public Resource addResource(Resource resource) throws IOException {
        return delegate.addResource(resource);
    }

    @Override
    public Resource addResource(Resource resource, MavenCoordinates mavenid) throws IOException {
        return delegate.addResource(resource, mavenid);
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        return delegate.removeResource(identity);
    }

    @Override
    public Resource getResource(ResourceIdentity identity) {
        return delegate.getResource(identity);
    }

    @Override
    public Repository getFallbackRepository() {
        return delegate.getFallbackRepository();
    }

    @Reference
    void bindRuntime(Runtime runtime) {
        this.runtime = runtime;
    }
    
    void unbindRuntime(Runtime runtime) {
        this.runtime = null;
    }
}
