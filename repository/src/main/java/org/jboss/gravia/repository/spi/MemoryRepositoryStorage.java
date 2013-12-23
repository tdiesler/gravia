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
package org.jboss.gravia.repository.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.repository.RepositoryReader;
import org.jboss.gravia.repository.RepositoryStorage;
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
    private Repository repository;

    public MemoryRepositoryStorage(PropertiesProvider propertyProvider, Repository repository) {
        super(MemoryRepositoryStorage.class.getSimpleName(), true);
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    void setRepository(Repository repository) {
        this.repository = repository;
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
