package org.jboss.gravia.repository;

/**
 * A persistent repository.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-May-2012
 */
public interface PersistentRepository extends Repository {

    RepositoryStorage getRepositoryStorage();

    Repository getDelegate();
}
