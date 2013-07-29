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

import org.jboss.gravia.resource.Adaptable;
import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.resource.Resource;

/**
 * A repository that contains {@link Resource resources}.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public interface Repository extends Adaptable {

    /**
     * The property that defines the Maven Repository base URLs.
     */
    String PROPERTY_MAVEN_REPOSITORY_BASE_URLS = "org.jboss.gravia.repository.maven.base.urls";
    /**
     * The property that defines the repository storage directory.
     */
    String PROPERTY_REPOSITORY_STORAGE_DIR = "org.jboss.gravia.repository.storage.dir";
    /**
     * The property that defines the repository storage file.
     */
    String PROPERTY_REPOSITORY_STORAGE_FILE = "org.jboss.gravia.repository.storage.file";

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

    /** A configuration property provider */
    interface ConfigurationPropertyProvider {

        /** Get the list of configured base URLs */
        String getProperty(String key, String defaultValue);
    }
}
