package org.jboss.gravia.resource;


/**
 * A resource capability.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Adaptable {

    /**
     * Adapt the this type to the given type
     */
    <T> T adapt(Class<T> type);
}
