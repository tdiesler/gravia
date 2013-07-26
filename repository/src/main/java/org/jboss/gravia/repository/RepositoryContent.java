package org.jboss.gravia.repository;

import java.io.InputStream;

import org.jboss.gravia.resource.Resource;

/**
 * An accessor for the default content of a resource.
 *
 * All {@link Resource} objects which represent non-abstract resources in a
 * {@link Repository} must be adaptable to this interface.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2012
 */
public interface RepositoryContent {

    /**
     * Returns a new input stream to the default format of this resource.
     *
     * @return A new input stream for associated resource.
     */
    InputStream getContent();
}
