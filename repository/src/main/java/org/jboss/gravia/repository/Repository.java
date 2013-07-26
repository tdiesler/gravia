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
