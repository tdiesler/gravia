package org.jboss.gravia.repository.spi;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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

import org.jboss.gravia.repository.PersistentRepository;
import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.logging.Logger;

/**
 * An abstract {@link PersistentRepository}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public abstract class AbstractPersistentRepository extends AbstractRepository implements PersistentRepository {

    static final Logger LOGGER = Logger.getLogger(Repository.class.getPackage().getName());

    private final ConfigurationPropertyProvider propertyProvider;
    private final Repository delegate;
    private RepositoryStorage storage;

    public AbstractPersistentRepository(ConfigurationPropertyProvider propertyProvider) {
        this(propertyProvider, null);
    }

    public AbstractPersistentRepository(ConfigurationPropertyProvider propertyProvider, Repository delegate) {
        this.propertyProvider = propertyProvider;
        this.delegate = delegate;
    }

    protected abstract RepositoryStorage createRepositoryStorage(PersistentRepository repository, ConfigurationPropertyProvider propertyProvider);

    protected ConfigurationPropertyProvider getConfigurationPropertyProvider() {
        return propertyProvider;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T adapt(Class<T> type) {
        T result = null;
        if (type.isAssignableFrom(getClass())) {
            result = (T) this;
        } else if (RepositoryStorage.class.isAssignableFrom(type)) {
            result = (T) getRepositoryStorage();
        } else if (delegate != null) {
            result = delegate.adapt(type);
        }
        return result;
    }

    @Override
    public RepositoryStorage getRepositoryStorage() {
        if (storage == null) {
            storage = createRepositoryStorage(this, getConfigurationPropertyProvider());
        }
        return storage;
    }

    @Override
    public Repository getDelegate() {
        return delegate;
    }

    @Override
    public Map<Requirement, Collection<Capability>> findProviders(Collection<Requirement> reqs) {
        if (reqs == null)
            throw new IllegalArgumentException("Null reqs");

        Map<Requirement, Collection<Capability>> result = new HashMap<Requirement, Collection<Capability>>();
        for (Requirement req : reqs) {
            Collection<Capability> providers = findProviders(req);
            result.put(req, providers);
        }

        return result;
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        if (req == null)
            throw new IllegalArgumentException("Null req");

        // Try to find the providers in the storage
        Collection<Capability> providers = getRepositoryStorage().findProviders(req);

        // Try to find the providers in the delegate
        if (providers.isEmpty() && getDelegate() != null) {
            providers = new HashSet<Capability>();
            for (Capability cap : getDelegate().findProviders(req)) {
                Resource res = cap.getResource();
                ResourceIdentity resid = res.getIdentity();
                Resource storageResource = getRepositoryStorage().getResource(resid);
                if (storageResource == null) {
                    storageResource = getRepositoryStorage().addResource(res);
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
}