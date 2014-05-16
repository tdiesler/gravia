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
package org.jboss.gravia.utils;

import java.net.URL;
import java.util.List;

import org.jboss.gravia.resource.Capability;
import org.jboss.gravia.resource.ContentNamespace;
import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Resource;

/**
 * A utility class for resource operations.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 15-May-2014
 */
public final class ResourceUtils {

    // Hide ctor
    private ResourceUtils() {
    }

    public static String getRuntimeName(Resource resource) {
        Capability icap = resource.getIdentityCapability();
        String runtimeName = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_RUNTIME_NAME_ATTRIBUTE);
        if (runtimeName == null) {
            String mvnatt = (String) icap.getAttribute(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE);
            if (mvnatt != null) {
                MavenCoordinates mavenid = MavenCoordinates.parse(mvnatt);
                runtimeName = mavenid.getArtifactId() + "-" + mavenid.getVersion() + "." + mavenid.getType();
            }
        }
        if (runtimeName == null) {
            List<Capability> ccaps = resource.getCapabilities(ContentNamespace.CONTENT_NAMESPACE);
            IllegalArgumentAssertion.assertFalse(ccaps.isEmpty(), "Resource has not content capability");
            URL contentURL = (URL) ccaps.get(0).getAttribute(ContentNamespace.CAPABILITY_URL_ATTRIBUTE);
            runtimeName = contentURL != null ? contentURL.getFile() : null;
        }
        return runtimeName;
    }

    public static String getRequiredRuntimeName(Resource resource) {
        String runtimeName = getRuntimeName(resource);
        IllegalArgumentAssertion.assertTrue(runtimeName != null, "Cannot obtain runtime name for: " + resource);
        return runtimeName;
    }
}
