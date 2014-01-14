package org.jboss.gravia.provision;

import org.jboss.gravia.resource.Resource;

/**
 * The container specific handle to an installed resource.
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Jan-2014
 */
public interface ResourceHandle {

    Resource getResource();

    <T> T adapt(Class<T> type);

    void uninstall();
}