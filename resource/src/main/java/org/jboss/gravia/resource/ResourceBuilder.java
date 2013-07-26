/*
 * #%L
 * JBossOSGi Resolver API
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
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
