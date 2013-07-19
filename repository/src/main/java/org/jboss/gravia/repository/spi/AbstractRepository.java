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
import java.util.HashMap;
import java.util.Map;

import org.jboss.gravia.repository.Repository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.logging.Logger;

/**
 * An abstract  {@link Repository} that does nothing.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public abstract class AbstractRepository implements Repository {

    static final Logger LOGGER = Logger.getLogger(Repository.class.getPackage().getName());
    
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
        }
        return result;
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
}