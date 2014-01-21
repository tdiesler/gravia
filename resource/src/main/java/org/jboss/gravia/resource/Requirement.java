/*
 * #%L
 * Gravia :: Resource
 * %%
 * Copyright (C) 2010 - 2014 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
