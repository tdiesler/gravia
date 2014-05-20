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

import java.io.InputStream;
import java.net.URL;
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
     */
    Capability addIdentityCapability(ResourceIdentity identity);

    /**
     * Add the identity {@link Capability}.
     */
    Capability addIdentityCapability(String symbolicName, String version);

    /**
     * Add the identity {@link Capability}.
     */
    Capability addIdentityCapability(String symbolicName, Version version);

    /**
     * Add the identity {@link Capability} from the given maven coordinates.
     */
    Capability addIdentityCapability(MavenCoordinates mavenid);

    /**
     * Add the identity {@link Capability}.
     */
    Capability addIdentityCapability(String symbolicName, Version version, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a content capability
     */
    Capability addContentCapability(InputStream content);

    /**
     * Add a content capability
     */
    Capability addContentCapability(InputStream content, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a content capability
     */
    Capability addContentCapability(URL contentURL);

    /**
     * Add a content capability
     */
    Capability addContentCapability(URL contentURL, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Capability}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    Capability addCapability(String namespace, String nsvalue);

    /**
     * Add a {@link Capability}
     */
    Capability addCapability(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add an identity {@link Requirement}
     */
    Requirement addIdentityRequirement(String symbolicName);

    /**
     * Add an identity {@link Requirement}
     */
    Requirement addIdentityRequirement(String symbolicName, String versionRange);

    /**
     * Add an identity {@link Requirement}
     */
    Requirement addIdentityRequirement(String symbolicName, VersionRange versionRange);

    /**
     * Add an identity {@link Requirement}
     */
    Requirement addIdentityRequirement(String symbolicName, VersionRange versionRange, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Add a {@link Requirement}
     *
     * @param namespace The namespace
     * @param nsvalue The namespace value
     */
    Requirement addRequirement(String namespace, String nsvalue);

    /**
     * Add a {@link Requirement}
     */
    Requirement addRequirement(String namespace, Map<String, Object> atts, Map<String, String> dirs);

    /**
     * Return true if the resource is valid
     */
    boolean isValid();

    /**
     * Get the current resource state from the builder
     */
    Resource getMutableResource();

    /**
     * Get the final resource from the builder
     */
    Resource getResource();
}
