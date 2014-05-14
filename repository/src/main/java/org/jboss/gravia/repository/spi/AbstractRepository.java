package org.jboss.gravia.repository.spi;

/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.DefaultResourceBuilder;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceBuilder;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.IllegalArgumentAssertion;

/**
 * An abstract  {@link Repository} that does nothing.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public abstract class AbstractRepository implements Repository {

    private final PropertiesProvider propertiesProvider;
    private RepositoryStorage storage;
    private Repository fallback;

    public AbstractRepository(PropertiesProvider propertyProvider) {
        this.propertiesProvider = propertyProvider;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public PropertiesProvider getPropertiesProvider() {
        return propertiesProvider;
    }

    public RepositoryStorage getRepositoryStorage() {
        return storage;
    }

    public void setRepositoryStorage(RepositoryStorage storage) {
        IllegalArgumentAssertion.assertNotNull(storage, "storage");
        if (this.storage != null)
            throw new IllegalStateException("RepositoryStorage already set");
        this.storage = storage;
    }

    public Repository getFallbackRepository() {
        return fallback;
    }

    public void setFallbackRepository(Repository fallback) {
        IllegalArgumentAssertion.assertNotNull(fallback, "fallback");
        if (this.fallback != null)
            throw new IllegalStateException("Fallback repository already set");
        this.fallback = fallback;
    }

    public RepositoryReader getRepositoryReader() {
        return getRequiredRepositoryStorage().getRepositoryReader();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        } else if (RepositoryStorage.class.isAssignableFrom(type)) {
            result = (T) getRepositoryStorage();
        } else if (RepositoryReader.class.isAssignableFrom(type)) {
            result = (T) getRepositoryReader();
        } else if (fallback != null) {
            result = fallback.adapt(type);
        }
        return result;
    }

    @Override
    public Resource addResource(Resource res) {
        return getRequiredRepositoryStorage().addResource(res);
    }

    @Override
    public Resource addResource(Resource res, MavenCoordinates mavenid) {
        Capability icap = res.getIdentityCapability();
        String attkey = IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE;
        String attval = (String) icap.getAttributes().get(attkey);
        if (attval != null && !attval.equals(mavenid.toExternalForm()))
            throw new IllegalArgumentException("Resource already contains a " + attkey + " attribute: " + attval);

        ResourceBuilder builder = new DefaultResourceBuilder();
        for (Capability aux : res.getCapabilities(null)) {
            Capability cap = builder.addCapability(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
            if (IdentityNamespace.IDENTITY_NAMESPACE.equals(cap.getNamespace())) {
                cap.getAttributes().put(attkey, mavenid.toExternalForm());
            }
        }
        for (Requirement aux : res.getRequirements(null)) {
            builder.addRequirement(aux.getNamespace(), aux.getAttributes(), aux.getDirectives());
        }
        Resource rescopy = builder.getResource();
        return getRequiredRepositoryStorage().addResource(rescopy);
    }

    @Override
    public Resource removeResource(ResourceIdentity identity) {
        return getRequiredRepositoryStorage().removeResource(identity);
    }

    @Override
    public Resource getResource(ResourceIdentity identity) {
        return getRequiredRepositoryStorage().getResource(identity);
    }

    @Override
    public Map<Requirement, Collection<Capability>> findProviders(Collection<Requirement> reqs) {
        IllegalArgumentAssertion.assertNotNull(reqs, "reqs");

        Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
        for (Requirement req : reqs) {
            Collection<Capability> providers = findProviders(req);
            result.put(req, providers);
        }

        return result;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        IllegalArgumentAssertion.assertNotNull(req, "req");

        // Try to find the providers in the storage
        RepositoryStorage repositoryStorage = getRequiredRepositoryStorage();
        Collection<Capability> providers = repositoryStorage.findProviders(req);

        // Try to find the providers in the fallback
        if (providers.isEmpty() && getFallbackRepository() != null) {
            providers = new HashSet<Capability>();
            for (Capability cap : getFallbackRepository().findProviders(req)) {
                Resource res = cap.getResource();
                ResourceIdentity resid = res.getIdentity();
                Resource storageResource = repositoryStorage.getResource(resid);
                if (storageResource == null) {
                    storageResource = repositoryStorage.addResource(res);
                    for (Capability aux : storageResource.getCapabilities(req.getNamespace())) {
                        if (cap.getAttributes().equals(aux.getAttributes())) {
                            cap = aux;
                            break;
                        }
                    }
                }
                providers.add(cap);
            }
        }
        return Collections.unmodifiableCollection(providers);
    }

    private RepositoryStorage getRequiredRepositoryStorage() {
        if (storage == null)
            throw new IllegalStateException("RepositoryStorage not set");
        return storage;
    }
}
