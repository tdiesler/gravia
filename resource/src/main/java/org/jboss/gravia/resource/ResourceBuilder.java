/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2013 JBoss by Red Hat
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
 * A builder for resources.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
public interface ResourceBuilder {

    /**
     * Add the identity {@link Capability}.
     *
     * @param symbolicName The symbolic name
     * @param version The version
     */
    Capability addIdentityCapability(String symbolicName, String version);

    /**
     * Add the identity {@link Capability}.
     *
     * @param symbolicName The symbolic name
     * @param version The version
     */
    Capability addIdentityCapability(String symbolicName, Version version);

    /**
     * Add the identity {@link Capability}.
     *
     * @param symbolicName The symbolic name
     * @param version The version
     * @param atts The attributes
     * @param dirs The directives
     */
    Capability addIdentityCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Capability}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    Capability addCapability(String namespace, String nsvalue);

    /**
     * Add a {@link Capability}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    Capability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add an identity {@link Requirement}
     *
     * @param symbolicName The symbolic name
     * @param version The version range
     */
    Requirement addIdentityRequirement(String symbolicName, String version);

    /**
     * Add an identity {@link Requirement}
     *
     * @param symbolicName The symbolic name
     * @param version The version range
     */
    Requirement addIdentityRequirement(String symbolicName, VersionRange version);

    /**
     * Add an identity {@link Requirement}
     *
     * @param symbolicName The symbolic name
     * @param version The version range
     * @param atts The attributes
     * @param dirs The directives
     */
    Requirement addIdentityRequirement(String symbolicName, VersionRange version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    Requirement addRequirement(String namespace, String nsvalue);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param atts The attributes
     * @param dirs The directives
     */
    Requirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Get the final resource from the builder
     */
    Resource getResource();
}
