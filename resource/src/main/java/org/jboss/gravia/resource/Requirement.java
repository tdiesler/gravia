/*
 * #%L
 * Gravia Resource
 * %%
 * Copyright (C) 2013 JBoss by Red Hat
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
