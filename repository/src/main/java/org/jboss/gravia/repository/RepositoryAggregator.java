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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.gravia.repository.spi.AbstractRepository;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;

/**
 * A {@link Repository} aggregator.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public class RepositoryAggregator extends AbstractRepository {

    private final List<Repository> delegates;

    public RepositoryAggregator(Repository... delegates) {
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
