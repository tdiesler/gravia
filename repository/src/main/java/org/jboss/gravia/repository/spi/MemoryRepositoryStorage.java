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
package org.jboss.gravia.repository.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
import org.jboss.gravia.resource.DefaultMatchPolicy;
import org.jboss.gravia.resource.DefaultResourceStore;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * A {@link RepositoryStorage} that maintains its state in local memory
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MemoryRepositoryStorage extends DefaultResourceStore implements RepositoryStorage {

    private final AtomicLong increment = new AtomicLong();
    private final Repository repository;

    public MemoryRepositoryStorage(PropertiesProvider propertyProvider, Repository repository) {
        super(MemoryRepositoryStorage.class.getSimpleName(), new DefaultMatchPolicy());
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public RepositoryReader getRepositoryReader() {
        final Iterator<Resource> itres = getResources();
        return new RepositoryReader() {

            @Override
            public Map<String, String> getRepositoryAttributes() {
                HashMap<String, String> attributes = new HashMap<String, String>();
                attributes.put("name", getRepository().getName());
                attributes.put("increment", new Long(increment.get()).toString());
                return Collections.unmodifiableMap(attributes);
            }

            @Override
            public Resource nextResource() {
                return itres.hasNext() ? itres.next() : null;
            }

            @Override
            public void close() {
                // do nothing
            }
        };
    }

    @Override
    public Resource addResource(Resource res) {
        Resource result = super.addResource(res);
        increment.incrementAndGet();
        return result;
    }

    @Override
    public Resource removeResource(ResourceIdentity resid) {
        Resource result = super.removeResource(resid);
        increment.incrementAndGet();
        return result;
    }
}
