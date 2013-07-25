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
 * An identity {@link Requirement} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class IdentityRequirementBuilder extends DefaultRequirementBuilder {

    public IdentityRequirementBuilder(String symbolicName, String versionRange) {
        this(symbolicName, new VersionRange(versionRange), null, null);
    }

    public IdentityRequirementBuilder(String symbolicName, VersionRange version) {
        this(symbolicName, version, null, null);
    }

    public IdentityRequirementBuilder(String symbolicName, VersionRange version, Map<String, Object> atts, Map<String, String> dirs) {
        super(IdentityNamespace.IDENTITY_NAMESPACE, symbolicName);
        if (version != null) {
            getAttributes().put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        }
        if (atts != null) {
            getAttributes().putAll(atts);
        }
        if (dirs != null) {
            getDirectives().putAll(dirs);
        }
    }

    public IdentityRequirementBuilder(MavenCoordinates mavenid) {
        this(MavenResourceBuilder.getSymbolicName(mavenid), MavenResourceBuilder.getVersion(mavenid).toString());
        getAttributes().put(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid.toExternalForm());
    }
}
