/*
 * #%L
 * Gravia Repository
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
package org.jboss.gravia.repository;

import java.util.Collection;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;
import org.jboss.gravia.resource.ResourceIdentity;
import org.jboss.gravia.utils.ObjectNameFactory;

/**
 * A {@link Repository} MBean.
 *
 * @author thomas.diesler@jboss.com
 * @since 23-Dec-2013
 */
public interface RepositoryMBean {

    ObjectName OBJECT_NAME = ObjectNameFactory.create("org.jboss.gravia:type=Repository");

    /**
     * Get the name for this repository
     */
    String getName();

    /**
     * Find the capabilities that match the specified requirement.
     *
     * @param requirement The requirements for which matching capabilities
     *        should be returned. Must not be {@code null}.
     * @return A collection of matching capabilities for the specified requirements.
     *         If there are no matching capabilities an empty collection is returned.
     *         The returned collection is the property of the caller and can be modified by the caller.
     */
    Collection<Capability> findProviders(Requirement requirement);

    /**
     * Find the capabilities that match the specified requirements.
     *
     * @param requirements The requirements for which matching capabilities
     *        should be returned. Must not be {@code null}.
     * @return A map of matching capabilities for the specified requirements.
     *         Each specified requirement must appear as a key in the map. If
     *         there are no matching capabilities for a specified requirement,
     *         then the value in the map for the specified requirement must be
     *         an empty collection. The returned map is the property of the
     *         caller and can be modified by the caller.
     */
    Map<Requirement, Collection<Capability>> findProviders(Collection<Requirement> requirements);

    /**
     * Add a {@link Resource} to the associated {@link RepositoryStorage}
     */
    Resource addResource(Resource resource);

    /**
     * Remove a {@link Resource} grom the associated {@link RepositoryStorage}
     */
    Resource removeResource(ResourceIdentity identity);

    /**
     * Get a a {@link Resource} by {@link ResourceIdentity}
     */
    Resource getResource(ResourceIdentity identity);
}
