package org.jboss.gravia.resource;

import java.util.Map;

/**
 * A resource capability.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Capability {

    /**
     * Returns the namespace of this capability.
     *
     * @return The namespace of this capability.
     */
    String getNamespace();

    /**
     * Returns the directives of this capability.
     *
     * @return An unmodifiable map of directive names to directive values for
     *         this capability, or an empty map if this capability has no
     *         directives.
     */
    Map<String, String> getDirectives();

    /**
     * Get the value of the given directive
     * 
     * @return null if no such directive is associated with this capability
     */
    String getDirective(String key);
    
    /**
     * Returns the attributes of this capability.
     *
     * @return An unmodifiable map of attribute names to attribute values for
     *         this capability, or an empty map if this capability has no
     *         attributes.
     */
    Map<String, Object> getAttributes();

    /**
     * Get the value of the given attribute
     * 
     * @return null if no such attribute is associated with this capability
     */
    Object getAttribute(String key);
    
    /**
     * Returns the resource declaring this capability.
     *
     * @return The resource declaring this capability.
     */
    Resource getResource();
}
