package org.jboss.gravia.repository;

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

import org.jboss.gravia.repository.spi.AbstractPersistentRepository;
import org.jboss.gravia.runtime.spi.PropertiesProvider;

/**
 * The default {@link PersistentRepository}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class DefaultPersistentRepository extends AbstractPersistentRepository {

    public DefaultPersistentRepository(PropertiesProvider propertyProvider) {
        this(propertyProvider, null);
    }

    public DefaultPersistentRepository(PropertiesProvider propertyProvider, Repository delegate) {
        super(propertyProvider, delegate);
    }

    @Override
    protected RepositoryStorage createRepositoryStorage(PersistentRepository repository, PropertiesProvider propertyProvider) {
        return new DefaultPersistentRepositoryStorage(repository, propertyProvider);
    }
}
