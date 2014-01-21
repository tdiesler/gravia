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
 * A resource capability.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface Capability extends Adaptable {

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
