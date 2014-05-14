/*
 * #%L
 * Gravia :: Repository
 * %%
 * Copyright (C) 2012 - 2014 JBoss by Red Hat
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
package org.jboss.gravia.repository;

import org.jboss.gravia.resource.IdentityNamespace;
import org.jboss.gravia.resource.IdentityRequirementBuilder;
import org.jboss.gravia.resource.MavenCoordinates;
import org.jboss.gravia.resource.Requirement;
import org.jboss.gravia.utils.MavenUtils;

/**
 * An identity {@link Requirement} builder.
 *
 * @author thomas.diesler@jboss.com
 * @since 16-Jan-2012
 */
public class MavenIdentityRequirementBuilder extends IdentityRequirementBuilder {

    public MavenIdentityRequirementBuilder(MavenCoordinates mavenid) {
        super(MavenUtils.getSymbolicName(mavenid), MavenUtils.getVersion(mavenid).toString());
        getAttributes().put(IdentityNamespace.CAPABILITY_MAVEN_IDENTITY_ATTRIBUTE, mavenid.toExternalForm());
    }
}
