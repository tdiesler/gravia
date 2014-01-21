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

import org.jboss.gravia.repository.spi.AbstractRepository;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * The default {@link Repository}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class DefaultRepository extends AbstractRepository {

    public DefaultRepository(PropertiesProvider propertyProvider) {
        super(propertyProvider);
        setRepositoryStorage(new DefaultRepositoryStorage(propertyProvider, this));
        setFallbackRepository(new DefaultMavenDelegateRepository(propertyProvider));
    }

    public DefaultRepository(PropertiesProvider propertyProvider, RepositoryStorage storage, Repository fallback) {
        super(propertyProvider);
        setRepositoryStorage(storage);
        setFallbackRepository(fallback);
    }
}
