package org.jboss.gravia.repository;

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

import org.jboss.gravia.repository.spi.AbstractPersistentRepository;

/**
 * The default {@link PersistentRepository}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class DefaultPersistentRepository extends AbstractPersistentRepository {

    public DefaultPersistentRepository(ConfigurationPropertyProvider propertyProvider) {
        this(propertyProvider, null);
    }

    public DefaultPersistentRepository(ConfigurationPropertyProvider propertyProvider, Repository delegate) {
        super(propertyProvider, delegate);
    }

    @Override
    protected RepositoryStorage createRepositoryStorage(PersistentRepository repository, ConfigurationPropertyProvider propertyProvider) {
        return new DefaultPersistentRepositoryStorage(repository, propertyProvider);
    }
}