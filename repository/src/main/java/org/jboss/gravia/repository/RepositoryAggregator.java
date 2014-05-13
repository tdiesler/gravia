package org.jboss.gravia.repository;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.repository.spi.AbstractRepository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.ArgumentAssertion;

/**
 * A {@link Repository} aggregator.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class RepositoryAggregator extends AbstractRepository {

    private final List<Repository> delegates;

    public RepositoryAggregator(PropertiesProvider propertiesProvider, Repository... delegates) {
        super(propertiesProvider);
        ArgumentAssertion.assertNotNull(delegates, "delegates");
        this.delegates = Arrays.asList(delegates);
    }

    public List<Repository> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    @Override
    public Collection<Capability> findProviders(Requirement req) {
        for (Repository repo : delegates) {
            Collection<Capability> providers = repo.findProviders(req);
            if (!providers.isEmpty()) {
                return providers;
            }
        }
        return Collections.emptyList();
    }
}
