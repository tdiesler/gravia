package org.jboss.gravia.repository.spi;

/*
 * #%L
 * JBossOSGi Repository
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
