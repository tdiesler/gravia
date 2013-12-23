package org.jboss.gravia.repository;

import org.jboss.gravia.repository.spi.AbstractRepository;
import org.jboss.gravia.runtime.spi.PropertiesProvider;
import org.jboss.gravia.utils.NotNullException;

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



/**
 * A {@link Repository} aggregator.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class RepositoryBuilder {

    private final AbstractRepository repository;

    public RepositoryBuilder(PropertiesProvider propertyProvider) {
        repository = new DefaultRepository(propertyProvider);
    }

    public void setRepositoryStorage(RepositoryStorage storage) {
        NotNullException.assertValue(storage, "storage");
        repository.setRepositoryStorage(storage);
    }

    public void setRepositoryDelegate(Repository delegate) {
        NotNullException.assertValue(delegate, "delegate");
        repository.setFallbackRepository(delegate);
    }

    public Repository getRepository() {
        return repository;
    }
}
