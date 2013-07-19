package org.jboss.gravia.resource;

import java.util.Map;

/**
 * A resource requirement.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Requirement extends Adaptable {

    /**
     * Returns the namespace of this requirement.
     *
     * @return The namespace of this requirement.
     */
    String getNamespace();

    /**
     * Returns the directives of this requirement.
     *
     * @return An unmodifiable map of directive names to directive values for
     *         this requirement, or an empty map if this requirement has no
     *         directives.
     */
    Map<String, String> getDirectives();

    /**
     * Get the value of the given directive
     * 
     * @return null if no such directive is associated with this requirement
     */
    String getDirective(String key);
    
    /**
     * Returns the attributes of this requirement.
     *
     * <p>
     * Requirement attributes have no specified semantics and are considered
     * extra user defined information.
     *
     * @return An unmodifiable map of attribute names to attribute values for
     *         this requirement, or an empty map if this requirement has no
     *         attributes.
     */
    Map<String, Object> getAttributes();

    /**
     * Get the value of the given attribute
     * 
     * @return null if no such attribute is associated with this requirement
     */
    Object getAttribute(String key);
    
    /**
     * A flag indicating that this is an optional requirement.
     */
    boolean isOptional();

    /**
     * Returns the resource declaring this requirement.
     *
     * @return The resource declaring this requirement. This can be {@code null}
     *         if this requirement is synthesized.
     */
    Resource getResource();
}
